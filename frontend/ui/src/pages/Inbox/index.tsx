import React, {useEffect} from 'react';
import _, {connect, ConnectedProps} from 'react-redux';
import {RouteComponentProps} from 'react-router-dom';

import {User} from 'httpclient';
import {listConversations} from '../../actions/conversations';
import {StateModel} from '../../reducers';

import Messenger from './Messenger';
import {setPageTitle} from '../../services/pageTitle';

export type ConversationRouteProps = RouteComponentProps<{conversationId: string}>;

interface InboxProps {
  user: User;
}

const mapStateToProps = (state: StateModel) => {
  return {
    user: state.data.user,
  };
};

const mapDispatchToProps = {
  listConversations,
};

const connector = connect(mapStateToProps, mapDispatchToProps);

const ConversationContainer = (props: InboxProps & ConnectedProps<typeof connector>) => {
  useEffect(() => {
    props.listConversations();
    setPageTitle('Inbox');
  }, []);

  return <Messenger />;
};

export default connector(ConversationContainer);
