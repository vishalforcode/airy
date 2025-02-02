import {ContentWrapper} from 'components/wrapper/ContentWrapper';
import React, {Dispatch, SetStateAction} from 'react';
import styles from './index.module.scss';
import {ReactComponent as SearchIcon} from 'assets/images/icons/search.svg';
import {useTranslation} from 'react-i18next';

type EmptyStateProps = {
  setNewWebhook: Dispatch<SetStateAction<boolean>>;
};

export const EmptyState = (props: EmptyStateProps) => {
  const {setNewWebhook} = props;
  const {t} = useTranslation();

  return (
    <ContentWrapper
      transparent={false}
      content={
        <div className={styles.container}>
          <div className={styles.contentContainer}>
            <div className={styles.iconContainer}>
              <SearchIcon className={styles.searchIcon} />
            </div>
            <h1>{t('noWebhooks')}</h1>
            <span>
              {t('noWebhooksText')}
              <span onClick={() => setNewWebhook(true)} className={styles.subscribeButton}>
                {t('subscribe')}
              </span>
            </span>
          </div>
        </div>
      }
    />
  );
};
