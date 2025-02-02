import React, {useEffect, useState} from 'react';
import {useTranslation} from 'react-i18next';
import {connect, ConnectedProps} from 'react-redux';

import {connectFacebookChannel} from '../../../../../actions/channel';

import {Button, Input, LinkButton, InfoButton} from 'components';
import {ConnectChannelFacebookRequestPayload} from 'httpclient/src';
import {ReactComponent as ArrowLeftIcon} from 'assets/images/icons/arrowLeft.svg';

import styles from './FacebookConnect.module.scss';

import {CONNECTORS_CONNECTED_ROUTE} from '../../../../../routes/routes';
import {useCurrentChannel} from '../../../../../selectors/channels';
import {useNavigate} from 'react-router-dom';

const mapDispatchToProps = {
  connectFacebookChannel,
};

const connector = connect(null, mapDispatchToProps);

const FacebookConnect = (props: ConnectedProps<typeof connector>) => {
  const {connectFacebookChannel} = props;
  const channel = useCurrentChannel();
  const navigate = useNavigate();
  const {t} = useTranslation();
  const [id, setId] = useState(channel?.sourceChannelId || '');
  const [token, setToken] = useState(channel?.metadata?.pageToken || '');
  const [name, setName] = useState(channel?.metadata?.name || '');
  const [image, setImage] = useState(channel?.metadata?.imageUrl || '');
  const [buttonTitle, setButtonTitle] = useState(t('connectPage') || '');
  const [errorMessage, setErrorMessage] = useState('');

  const CONNECTED_ROUTE = CONNECTORS_CONNECTED_ROUTE;

  const buttonStatus = () => {
    return !(id.length > 5 && token != '');
  };

  useEffect(() => {
    if (channel) {
      setButtonTitle(t('updatePage'));
    }
  }, []);

  const connectNewChannel = () => {
    const connectPayload: ConnectChannelFacebookRequestPayload = {
      pageId: id,
      pageToken: token,
      ...(name &&
        name !== '' && {
          name,
        }),
      ...(image &&
        image !== '' && {
          imageUrl: image,
        }),
    };

    connectFacebookChannel(connectPayload)
      .then(() => {
        navigate(CONNECTED_ROUTE + '/facebook', {replace: true});
      })
      .catch(() => {
        setErrorMessage(t('errorMessage'));
      });
  };

  return (
    <div className={styles.wrapper}>
      <h1 className={styles.headline}>Facebook Messenger</h1>
      <div>
        <InfoButton link="https://airy.co/docs/core/sources/facebook" text={t('infoButtonText')} color="grey" />

        <LinkButton onClick={() => navigate(-1)} type="button">
          <ArrowLeftIcon className={styles.backIcon} />
          {t('back')}
        </LinkButton>
      </div>
      <div className={styles.inputContainer}>
        <Input
          id="id"
          label={t('facebookPageId')}
          placeholder={t('facebookPageIdPlaceholder')}
          value={id}
          onChange={(event: React.ChangeEvent<HTMLInputElement>) => setId(event.target.value)}
          minLength={6}
          required={true}
          height={32}
          hint={errorMessage}
          fontClass="font-base"
        />
        <Input
          id="token"
          label={t('token')}
          placeholder={t('tokenPlaceholder')}
          value={token}
          onChange={(event: React.ChangeEvent<HTMLInputElement>) => setToken(event.target.value)}
          required={true}
          height={32}
          hint={errorMessage}
          fontClass="font-base"
        />
        <Input
          id="name"
          label={t('nameOptional')}
          placeholder={t('addAName')}
          hint={t('nameFacebookPlaceholder')}
          value={name}
          onChange={(event: React.ChangeEvent<HTMLInputElement>) => setName(event.target.value)}
          height={32}
          fontClass="font-base"
        />
        <Input
          id="image"
          label={t('imageUrlOptional')}
          placeholder={t('addAnUrl')}
          hint={t('imageFacebookHint')}
          value={image}
          onChange={(event: React.ChangeEvent<HTMLInputElement>) => setImage(event.target.value)}
          height={32}
          fontClass="font-base"
        />
      </div>
      <Button styleVariant="normal" disabled={buttonStatus()} onClick={() => connectNewChannel()}>
        {buttonTitle}
      </Button>
    </div>
  );
};

export default connector(FacebookConnect);
