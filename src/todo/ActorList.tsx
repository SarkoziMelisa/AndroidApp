import React, {useContext, useState} from 'react';
import {RouteComponentProps} from 'react-router';
import {
    IonButton,
    IonContent,
    IonFab,
    IonFabButton,
    IonHeader,
    IonIcon,
    IonList,
    IonLoading,
    IonPage,
    IonTitle,
    IonToolbar,
    IonButtons,
    IonInfiniteScroll,
    IonInfiniteScrollContent,
    IonSearchbar,
    useIonViewDidEnter
} from '@ionic/react';
import { add } from 'ionicons/icons';
import Actor from './Actor';
import { getLogger, Storage } from '../core';
import { ActorContext } from './ActorProvider';
import {useNetwork} from "../core/useNetwork";

const log = getLogger('ActorsList');

interface ActorListProps extends RouteComponentProps<{
    id?: string;
}> {}

const ActorList: React.FC<ActorListProps> = ({ history }) => {
    const { actors, filterActor, filterString, pageActor, currentPageNumber, fetching, fetchingError} = useContext(ActorContext);
    const [disableInfiniteScroll, setDisableInfiniteScroll] = useState<boolean>(false);

    const { networkStatus } = useNetwork();

    async function searchNext($event: CustomEvent<void>) {
        if (pageActor) {
            await pageActor(currentPageNumber + 1)
        }
        ($event.target as HTMLIonInfiniteScrollElement).complete();
    }

    useIonViewDidEnter(async () => {

    });

    const handleLogout = () => {
        Storage.clear();
        window.location.reload();
    };

    log('render');
    return (
        <IonPage>
            <IonHeader>
                <IonToolbar>
                    {
                        <div slot="start" className={`circle ${networkStatus.connected ? "connected" : "disconnected"}`} />
                    }
                    <IonTitle>Actor list</IonTitle>
                    <IonButtons slot="end">
                        <IonButton onClick={handleLogout}>
                            Logout
                        </IonButton>
                    </IonButtons>
                </IonToolbar>
            </IonHeader>
            <IonContent>
                <IonSearchbar
                    value={ filterString }
                    debounce={100}
                    disabled={!networkStatus.connected}
                    onIonChange={e => filterActor && filterActor(e.detail.value!) && setDisableInfiniteScroll(false)}>
                </IonSearchbar>
                <IonLoading isOpen={fetching} message="Fetching actors" />
                    { actors && (
                        <IonList>
                            { actors.map(({_id, name, wikipediaLink, matchingPercentage}) =>
                                <Actor key={_id} _id={_id} name={name} wikipediaLink={wikipediaLink}
                                       matchingPercentage={matchingPercentage}
                                       onEdit={id => history.push(`/actor/${id}`)}/>
                            )};
                        </IonList>
                    )}

                <IonInfiniteScroll threshold="100px" disabled={ !networkStatus.connected }
                                   onIonInfinite={(e: CustomEvent<void>) => searchNext(e)}>
                    <IonInfiniteScrollContent
                        loadingText="Loading...">
                    </IonInfiniteScrollContent>
                </IonInfiniteScroll>

                {fetchingError && (
                    <div>{fetchingError.message || 'Failed to fetch actors'}</div>
                )}

                <IonFab vertical="bottom" horizontal="end" slot="fixed">
                    <IonFabButton onClick={() => history.push('/actor')}>
                        <IonIcon icon={add} />
                    </IonFabButton>
                </IonFab>

            </IonContent>
        </IonPage>
    );
};

export default ActorList;
