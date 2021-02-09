import {FacebookRender} from './providers/facebook/FacebookRender';
import {ChatPluginRender} from './providers/chatplugin/ChatPluginRender';
import {MessageRenderProps} from './shared';

type Provider = (messageRenderProps: MessageRenderProps) => JSX.Element;

export const renderProviders: {[key: string]: Provider} = {
  facebook: FacebookRender,
  chat_plugin: ChatPluginRender,
};
