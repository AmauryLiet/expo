import {
  createContext,
  use,
  useCallback,
  useEffect,
  useState,
  type PropsWithChildren,
} from 'react';
import { StyleSheet } from 'react-native';

import {
  NativeModalPortalContent,
  NativeModalPortalContentWrapper,
  NativeModalPortalHost,
} from './native';

interface PortalContextType {
  hasHostId: (hostId: string) => boolean;
  addHostId: (hostId: string) => void;
  removeHostId: (hostId: string) => void;
}

export const PortalContext = createContext<PortalContextType>({
  hasHostId: () => false,
  addHostId: () => {},
  removeHostId: () => {},
});

export const PortalContextProvider = (props: PropsWithChildren) => {
  const [nativeIds, setNativeIds] = useState<Set<string>>(() => new Set());

  const hasHostId = useCallback(
    (hostId: string) => {
      return nativeIds.has(hostId);
    },
    [nativeIds]
  );

  const addHostId = useCallback((hostId: string) => {
    setNativeIds((prev) => new Set(prev).add(hostId));
  }, []);

  const removeHostId = useCallback((hostId: string) => {
    setNativeIds((prev) => {
      const updated = new Set(prev);
      updated.delete(hostId);
      return updated;
    });
  }, []);

  return (
    <PortalContext.Provider value={{ hasHostId, addHostId, removeHostId }}>
      {props.children}
    </PortalContext.Provider>
  );
};

export interface ModalPortalHostProps {
  hostId: string;
  // When set to true, the portal host will take up the full available space.
  fluid?: boolean;
}

export const ModalPortalHost = (props: ModalPortalHostProps) => {
  const { hasHostId, addHostId, removeHostId } = use(PortalContext);
  useEffect(() => {
    if (hasHostId(props.hostId)) {
      throw new Error(
        `ModalPortalHost with hostId "${props.hostId}" already exists. Each hostId must be unique.`
      );
    }
    addHostId(props.hostId);
    return () => {
      removeHostId(props.hostId);
    };
  }, [props.hostId]);
  return (
    <NativeModalPortalHost
      style={{ flex: props.fluid ? 1 : undefined }}
      fluid={props.fluid}
      hostId={props.hostId}
    />
  );
};

export interface ModalPortalContentProps {
  hostId: string;
  children: React.ReactNode;
}

export const ModalPortalContent = (props: ModalPortalContentProps) => {
  const { hasHostId } = use(PortalContext);
  const isHostFound = hasHostId(props.hostId);
  // At first render, the hostId might not be registered yet
  if (!isHostFound) {
    return null;
  }
  return (
    <NativeModalPortalContentWrapper hostId={props.hostId}>
      {isHostFound ? (
        <NativeModalPortalContent style={styles.portalContent}>
          {props.children}
        </NativeModalPortalContent>
      ) : null}
    </NativeModalPortalContentWrapper>
  );
};

const styles = StyleSheet.create({
  portalContent: {
    position: 'absolute',
  },
});
