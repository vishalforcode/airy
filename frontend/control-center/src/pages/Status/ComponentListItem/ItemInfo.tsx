import React, {useState} from 'react';
import {StateModel} from '../../../reducers';
import {enableDisableComponent} from '../../../actions/config';
import {ReactComponent as CheckmarkIcon} from 'assets/images/icons/checkmarkFilled.svg';
import {ReactComponent as UncheckedIcon} from 'assets/images/icons/uncheckIcon.svg';
import {ReactComponent as ArrowRight} from 'assets/images/icons/arrowRight.svg';
import {getChannelAvatar} from '../../../components/ChannelAvatar';
import {getComponentName} from '../../../services';
import {getSourceForComponent} from 'model';
import {SettingsModal, Button, Toggle, Tooltip} from 'components';
import styles from './index.module.scss';
import {connect, ConnectedProps, useSelector} from 'react-redux';
import {useTranslation} from 'react-i18next';

type ComponentInfoProps = {
  healthy: boolean;
  itemName: string;
  isComponent: boolean;
  isExpanded: boolean;
  enabled?: boolean;
  setIsPopUpOpen: (value: boolean) => void;
} & ConnectedProps<typeof connector>;

const mapDispatchToProps = {
  enableDisableComponent,
};

const connector = connect(null, mapDispatchToProps);

const formatName = (name: string) => {
  if (name?.includes('enterprise')) {
    name = name.replace('enterprise-', '');
  }
  if (name?.includes('sources')) {
    name = name.replace('sources-', '');
  }
  return name;
};

const isConfigurableConnector = (name: string) => {
  return (
    (name.includes('sources') || name.includes('enterprise')) &&
    !name.includes('salesforce') &&
    !name.includes('mobile') &&
    !name.includes('zendesk')
  );
};

const ItemInfo = (props: ComponentInfoProps) => {
  const connectors = useSelector((state: StateModel) => state.data.connector);
  const {healthy, itemName, isComponent, isExpanded, enabled, setIsPopUpOpen, enableDisableComponent} = props;
  const [channelSource] = useState(itemName && getSourceForComponent(itemName));
  const [componentName] = useState(itemName && getComponentName(itemName));
  const [componentEnabled, setComponentEnabled] = useState(enabled);
  const [enablePopupVisible, setEnablePopupVisible] = useState(false);
  const isVisible = isExpanded || isComponent;
  const {t} = useTranslation();
  const connectorInstalltionConfig =
    connectors[formatName(itemName)] && Object.keys(connectors[formatName(itemName)]).length > 0;

  const triggerEnableDisableAction = (enabled: boolean) => {
    enableDisableComponent({components: [{name: itemName, enabled: enabled}]});
    setComponentEnabled(enabled);
    setEnablePopupVisible(false);
    setIsPopUpOpen(false);
  };

  const onEnableComponent = (enabled: boolean) => {
    if (enabled) {
      triggerEnableDisableAction(enabled);
      setIsPopUpOpen(false);
      return;
    }

    setEnablePopupVisible(true);
    setIsPopUpOpen(true);
  };

  return (
    <>
      {isVisible && (
        <div className={`${styles.container} ${!isComponent ? styles.expandedContainer : ''}`}>
          <div className={styles.name}>
            {isComponent ? (
              <>
                <div
                  className={`${styles.arrowDownIcon} ${
                    isExpanded ? styles.arrowDownIconOpen : styles.arrowDownIconClose
                  }`}
                >
                  <ArrowRight width={8} />
                </div>
                <div className={styles.icons}>{getChannelAvatar(channelSource)}</div>
              </>
            ) : (
              <div className={styles.blankSpace} />
            )}

            <p className={`${isComponent ? styles.componentName : styles.serviceName}`}>
              {isComponent ? componentName : itemName}
            </p>
          </div>

          <div className={styles.healthyStatus}>
            {isComponent && isConfigurableConnector(itemName) && enabled && !connectorInstalltionConfig ? (
              <Tooltip
                hoverElement={<UncheckedIcon className={`${styles.icons} ${styles.installedNotConfigured}`} />}
                hoverElementHeight={20}
                hoverElementWidth={20}
                tooltipContent={t('needsConfiguration')}
              />
            ) : healthy && enabled ? (
              <Tooltip
                hoverElement={<CheckmarkIcon className={styles.icons} />}
                hoverElementHeight={20}
                hoverElementWidth={20}
                tooltipContent={t('healthy')}
              />
            ) : !healthy && enabled ? (
              <Tooltip
                hoverElement={<UncheckedIcon className={`${styles.icons} ${styles.unhealthy}`} />}
                hoverElementHeight={20}
                hoverElementWidth={20}
                tooltipContent={t('notHealthy')}
              />
            ) : (
              <Tooltip
                hoverElement={<UncheckedIcon className={`${styles.icons} ${styles.disabledHealthy}`} />}
                hoverElementHeight={20}
                hoverElementWidth={20}
                tooltipContent={t('disabled')}
              />
            )}
          </div>

          {isComponent && (
            <div className={styles.enabled}>
              <Toggle value={componentEnabled} updateValue={onEnableComponent} size="small" variant="green" />
            </div>
          )}
        </div>
      )}

      {enablePopupVisible && (
        <SettingsModal
          wrapperClassName={styles.enableModalContainerWrapper}
          containerClassName={styles.enableModalContainer}
          title={t('disableComponent') + ' ' + componentName}
          close={() => {
            setEnablePopupVisible(false);
            setIsPopUpOpen(false);
          }}
        >
          <p className={styles.popUpSubtitle}>{t('disableComponentText')}</p>
          <Button
            styleVariant="normal"
            style={{padding: '0 60Px'}}
            type="submit"
            onClick={() => triggerEnableDisableAction(false)}
          >
            {t('disableComponent')}
          </Button>
        </SettingsModal>
      )}
    </>
  );
};

export default connector(ItemInfo);
