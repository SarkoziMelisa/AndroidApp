import axios from 'axios';
import { authConfig, baseUrl, getLogger, withLogs } from '../core';
import { ActorProps } from './ActorProps';

const actorURL = `http://${baseUrl}/api/actor`;

export const getActors: (token: string, queryString: string) => Promise<ActorProps[]> = (token, queryString) => {
    return withLogs(axios.get(`${actorURL}${queryString}`, authConfig(token)), 'createActor');
}

export const createActor: (token: string, actor: ActorProps) => Promise<ActorProps[]> = (token, actor) => {
    return withLogs(axios.post(actorURL, actor, authConfig(token)), 'createActor');
}

export const updateActor: (token: string, actor: ActorProps) => Promise<ActorProps[]> = (token, actor) => {
    return withLogs(axios.put(`${actorURL}/${actor._id}`, actor, authConfig(token)), 'updateActor');
}

export const deleteActor: (token: string, actor: ActorProps) => Promise<ActorProps[]> = (token, actor) => {
    return withLogs(axios.delete(`${actorURL}/${actor._id}`, authConfig(token)), 'deleteActor');
}

interface MessageData {
    type: string;
    payload: ActorProps;
}

const log = getLogger('ws');

export const newWebSocket = (token: string, onMessage: (data: MessageData) => void) => {
    const ws = new WebSocket(`ws://${baseUrl}`);
    ws.onopen = () => {
        log('web socket onopen');
        ws.send(JSON.stringify({ type: 'authorization', payload: { token } }));
    };
    ws.onclose = () => {
        log('web socket onclose');
    };
    ws.onerror = error => {
        log('web socket onerror', error);
    };
    ws.onmessage = messageEvent => {
        log('web socket onmessage');
        onMessage(JSON.parse(messageEvent.data));
    };
    return () => {
        ws.close();
    }
}
