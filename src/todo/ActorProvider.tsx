import React, { useCallback, useContext, useEffect, useReducer } from 'react';
import PropTypes from 'prop-types';
import { getLogger, Storage } from '../core';
import { ActorProps } from './ActorProps';
import { createActor, getActors, newWebSocket, updateActor, deleteActor } from './ActorApi';
import { AuthContext } from '../auth';
import {Plugins} from "@capacitor/core";
import {useNetwork} from "../core/useNetwork";

const log = getLogger('ActorProvider');

type SaveActorFn = (actor: ActorProps) => Promise<any>;
type DeleteActorFn = (actor: ActorProps) => Promise<any>;
type FilterActorFn = (string: string) => Promise<any>;
type PagingActorFn = (page: number) => Promise<any>;

const { BackgroundTask } = Plugins;

export interface ActorsState {
    actors?: ActorProps[],
    fetching: boolean,
    fetchingError?: Error | null,
    saving: boolean,
    savingError?: Error | null,
    saveActor?: SaveActorFn,
    removeActor?: DeleteActorFn,
    deleting: boolean,
    deletingError?: Error | null,
    currentPageNumber: number
    filterString: string,
    filterActor?: FilterActorFn,
    pageActor?: PagingActorFn,
    usingLocal: boolean
}

interface ActionProps {
    type: string,
    payload?: any,
}

const initialState: ActorsState = {
    fetching: false,
    saving: false,
    deleting: false,
    currentPageNumber: 1,
    filterString: "",
    usingLocal: false
};

const FETCH_ACTORS_STARTED = 'FETCH_ACTORS_STARTED';
const FETCH_ACTORS_SUCCEEDED = 'FETCH_ACTORS_SUCCEEDED';
const FETCH_ACTORS_FAILED = 'FETCH_ACTORS_FAILED';
const SAVE_ACTOR_STARTED = 'SAVE_ACTOR_STARTED';
const SAVE_ACTOR_SUCCEEDED = 'SAVE_ACTOR_SUCCEEDED';
const SAVE_ACTOR_FAILED = 'SAVE_ACTOR_FAILED';
const SAVE_ACTOR_CONFLICT = 'SAVE_ACTOR_CONFLICT';
const DELETE_ACTOR_STARTED = 'DELETE_ACTOR_STARTED';
const DELETE_ACTOR_SUCCEEDED = 'DELETE_ACTOR_SUCCEEDED';
const DELETE_ACTOR_FAILED = 'DELETE_ACTOR_FAILED';


const reducer: (state: ActorsState, action: ActionProps) => ActorsState =
    (state, { type, payload }) => {
        switch (type) {
            case FETCH_ACTORS_STARTED:
                let pageNr = 1;
                let filter = state.filterString;
                if(payload && payload.page)
                    pageNr = payload.page;
                if(payload && payload.query !== undefined)
                    filter = payload.query;
                return { ...state, currentPageNumber: pageNr, filterString: filter, fetching: true, fetchingError: null, usingLocal: false };
            case FETCH_ACTORS_SUCCEEDED:
                if(state.currentPageNumber === 1)
                    return { ...state, actors: payload.actors, fetching: false, usingLocal: false};
                else
                    return { ...state, actors: state.actors?.concat(payload.actors), fetching: false, usingLocal: false};
            case FETCH_ACTORS_FAILED:
                return { ...state, fetchingError: payload.error, fetching: false };
            case SAVE_ACTOR_STARTED:
                return { ...state, savingError: null, saving: true };
            case SAVE_ACTOR_SUCCEEDED:
                const actors = [...(state.actors || [])];
                const actor = payload.actor;
                const index = actors.findIndex(it => it._id === actor._id);
                if (index === -1) {
                    actors.splice(0, 0, actor);
                } else {
                    actors[index] = actor;
                }
                return { ...state, actors, saving: false, usingLocal: payload.local };
            case SAVE_ACTOR_CONFLICT:
                const actorsSaved = [...(state.actors || [])];
                const actorSaved = payload.actor;
                const i = actorsSaved.findIndex(it => it._id === actorSaved._id);
                if (i === -1) {
                    actorsSaved.splice(0, 0, actorSaved);
                } else {
                    actorsSaved[i] = actorSaved;
                }
                return { ...state, actors: actorsSaved, savingError: payload.error, saving: false, usingLocal: payload.local };
            case SAVE_ACTOR_FAILED:
                return { ...state, savingError: payload.error, saving: false };
            case DELETE_ACTOR_STARTED:
                return { ...state, deleting: true };
            case DELETE_ACTOR_SUCCEEDED:
                log('deleting succeeded');
                const _actors = [...(state.actors || [])];
                const _actor = payload.actor;
                const _index = _actors.findIndex(it => it._id === _actor._id);
                if (_index !== -1) {
                    _actors.splice(_index, 1);
                }
                return { ...state, _actors, deleting: false, usingLocal: payload.local };
            case DELETE_ACTOR_FAILED:
                return { ...state, deleting: false, deletingError: payload.error };
            default:
                return state;
        }
    };

export const ActorContext = React.createContext<ActorsState>(initialState);

interface ActorProviderProps {
    children: PropTypes.ReactNodeLike,
}

export const ActorProvider: React.FC<ActorProviderProps> = ({ children }) => {
    const { token } = useContext(AuthContext);
    const { networkStatus } = useNetwork();

    const [state, dispatch] = useReducer(reducer, initialState);
    const { actors, filterString, usingLocal, currentPageNumber, fetching, fetchingError, saving, savingError, deleting, deletingError} = state;

    useEffect(getActorsEffect, [token]);
    useEffect(wsEffect, [token]);
    useEffect(backgroundTask, [token, networkStatus.connected]);
    useEffect(setActors,[token, actors]);

    const saveActor = useCallback<SaveActorFn>(saveActorCallback, [token]);
    const removeActor = useCallback<DeleteActorFn>(deleteActorCallback, [token]);
    const filterActor = useCallback<FilterActorFn>(filterActorCallback, [token, filterString]);
    const pageActor = useCallback<PagingActorFn>(pageActorCallback, [token, currentPageNumber, filterString]);

    const value = { actors, filterString, currentPageNumber, usingLocal, fetching, fetchingError, saving, savingError, saveActor, removeActor, deleting, deletingError, pageActor, filterActor};
    let savedLocallyNumber = 1;

    log('returns');
    return (
        <ActorContext.Provider value={value}>
            {children}
        </ActorContext.Provider>
    );

    function setActors(){
        setActorsStorage();
        async function setActorsStorage() {
            await Storage.set({key: "actors", value: JSON.stringify(actors)});
        }
    }

    function backgroundTask(){
        if(networkStatus.connected){
            let taskId = BackgroundTask.beforeExit(async () => {
                console.log('useBackgroundTask - executeTask started');

                const ret = await Storage.get({key: "actors"});
                console.log(ret.value);
                if(ret.value != null && ret.value !== 'undefined') {
                    const actors = JSON.parse(ret.value);
                    for(let i = 0; i < actors.length; i++){
                        const actor = actors[i];
                        const id = String(actor._id);
                        await (id.includes("saved") ? createActor(token, actor) : updateActor(token, actor));
                    }
                }
                console.log('useBackgroundTask - executeTask finished');
                BackgroundTask.finish({ taskId });
            });
        }
        return;
    }

    function getActorsEffect() {
        let canceled = false;
        fetchActors();
        return () => {
            canceled = true;
        };

        async function fetchActors() {
            if (!token?.trim()) {
                return;
            }
            if (!networkStatus.connected) {
                const ret = await Storage.get({key: "actors"});
                if (ret.value != null && ret.value !== 'undefined') {
                    const actors = JSON.parse(ret.value);
                    dispatch({type: FETCH_ACTORS_SUCCEEDED, payload: {actors}});
                    return;
                }
            } else {
                try {
                    log('fetchActors started');
                    dispatch({type: FETCH_ACTORS_STARTED});

                    const actors = await getActors(token, filterString === "" ? `?page=1` : `name=${filterString}?page=1`);

                    if (!canceled) {
                        dispatch({type: FETCH_ACTORS_SUCCEEDED, payload: {actors}});
                    }
                    log('fetchActors succeeded');
                } catch (error) {
                    log('fetchActors failed');
                    if(error.message === "Network Error"){
                        const ret = await Storage.get({key: "actors"});
                        if(ret.value != null && ret.value !== 'undefined'){
                            const actors = JSON.parse(ret.value);
                            dispatch({ type: FETCH_ACTORS_SUCCEEDED, payload: { actors } });
                            return;
                        }
                        dispatch({ type: FETCH_ACTORS_SUCCEEDED, payload: { actors } });
                    }
                    else {
                        dispatch({type: FETCH_ACTORS_FAILED, payload: {error}});
                    }
                }
            }
        }
    }

    async function pageActorCallback(page: number){
        if (!token?.trim()) {
            return;
        }
        if(!networkStatus.connected){
            log('filterActors failed');
            dispatch({ type: FETCH_ACTORS_FAILED, payload: { error: new Error("Not connected") } });
            return;
        }
        try {
            log('nextPage started');
            dispatch({ type: FETCH_ACTORS_STARTED, payload: { page: page} });
            const issues = await getActors(token, `?name=${filterString}&page=${page}`);
            log('nextPage succeeded');
            dispatch({ type: FETCH_ACTORS_SUCCEEDED, payload: { issues } });
        }
        catch (error) {
            log('nextPage failed');
            dispatch({ type: FETCH_ACTORS_FAILED, payload: { error } });
        }
    }

    async function filterActorCallback(query: string){
        if (!token?.trim()) {
            return;
        }
        if(!networkStatus.connected){
            log('filterActors failed');
            dispatch({ type: FETCH_ACTORS_STARTED, payload: { query: query} });
            dispatch({ type: FETCH_ACTORS_FAILED, payload: { error: new Error("Not connected") } });
            return;
        }
        try {
            log('filterActors started');
            dispatch({ type: FETCH_ACTORS_STARTED, payload: { query: query} });
            const actors = await getActors(token, `?name=${filterString}&page=1`);
            log('filterIssues succeeded');
            dispatch({ type: FETCH_ACTORS_SUCCEEDED, payload: { actors } });
        }
        catch (error) {
            log('filterActors failed');
            dispatch({ type: FETCH_ACTORS_FAILED, payload: { error } });
        }
    }

    async function saveActorCallback(actor: ActorProps) {
        if(!networkStatus.connected){
            if(actor.name) {
                dispatch({ type: SAVE_ACTOR_STARTED });
                if(!actor._id){
                    actor._id = `saved${savedLocallyNumber}`;
                    savedLocallyNumber += 1;
                }
                else{
                    actor.version = actor.version ? actor.version +1 : 1;
                }
                dispatch({type: SAVE_ACTOR_SUCCEEDED, payload: {actor: actor, local: true}});
            }
            else
                dispatch({ type: SAVE_ACTOR_FAILED, payload: { error: new Error("Bad Request") } });
        }
        else {
            try {
                log('saveActor started');
                dispatch({type: SAVE_ACTOR_STARTED});
                const savedActor = await (actor._id ? updateActor(token, actor) : createActor(token, actor));
                log('saveActor succeeded');

                dispatch({ type: SAVE_ACTOR_SUCCEEDED, payload: { actor: savedActor, local: false } });
            } catch (error) {
                log('saveActor failed');
                log(error);
                if(error.message === 'Network Error'){
                    if(actor.name) {
                        if(!actor._id){
                            actor._id = `saved${savedLocallyNumber}`;
                            savedLocallyNumber += 1;
                        }
                        else{
                            actor.version = actor.version ? actor.version +1 : 1;
                        }
                        dispatch({type: SAVE_ACTOR_SUCCEEDED, payload: {actor: actor, local: true}});
                    }
                    else
                        dispatch({ type: SAVE_ACTOR_FAILED, payload: { error: new Error("Bad Request") } });
                }
                else if(error.message === "Request failed with status code 409"){
                    alert("You have conflicts");
                    var actorError = error.response.data.actor;
                    actor._id = actorError._id;
                    actor.name = actorError.name;
                    actor.wikipediaLink = actorError.wikipediaLink;
                    actor.matchingPercentage = actorError.matchingPercentage;
                    actor.version = actorError.version;
                    dispatch({ type: SAVE_ACTOR_CONFLICT, payload: { error: new Error("Version conflict"), actor: actor } });
                }
                else {
                    dispatch({type: SAVE_ACTOR_FAILED, payload: {error}});
                }
            }
        }
    }

    async function deleteActorCallback(actor: ActorProps) {
        if(!networkStatus.connected){
            dispatch({ type: DELETE_ACTOR_STARTED });
            dispatch({ type: DELETE_ACTOR_SUCCEEDED, payload: { actor: actor, local: true } });
        }
        else {
            try {
                log('deleteActor started');
                dispatch({type: DELETE_ACTOR_STARTED});
                const deletedActor = await deleteActor(token, actor);
                log('deleteActor succeeded');
                dispatch({type: DELETE_ACTOR_SUCCEEDED, payload: {actor: actor}});
            } catch (error) {
                log('deleteActor failed');
                if(error.message === "Network Error"){
                    dispatch({ type: DELETE_ACTOR_SUCCEEDED, payload: { actor: actor, local: true} });
                }
                else {
                    dispatch({type: DELETE_ACTOR_FAILED, payload: {error}});
                }
            }
        }
    }

    function wsEffect() {
        let canceled = false;
        log('wsEffect - connecting');
        let closeWebSocket: () => void;
        if (token?.trim()) {
            closeWebSocket = newWebSocket(token, message => {
                if (canceled) {
                    return;
                }
                const { type, payload: actor } = message;
                log(`ws message, actor ${type}`);
                if (type === 'created' || type === 'updated') {
                    dispatch({ type: SAVE_ACTOR_SUCCEEDED, payload: { actor } });
                }
                if (type === 'deleted') {
                    dispatch({type: DELETE_ACTOR_SUCCEEDED, payload: {actor}});
                }
            });
        }
        return () => {
            log('wsEffect - disconnecting');
            canceled = true;
            closeWebSocket?.();
        }
    }
};
