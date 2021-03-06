package com.thoughtworks.selenium.grid.hub;

import com.thoughtworks.selenium.grid.configuration.EnvironmentConfiguration;
import com.thoughtworks.selenium.grid.configuration.GridConfiguration;
import com.thoughtworks.selenium.grid.configuration.HubConfiguration;
import com.thoughtworks.selenium.grid.configuration.ResourceLocator;
import com.thoughtworks.selenium.grid.hub.management.LifecycleManager;
import com.thoughtworks.selenium.grid.hub.remotecontrol.DynamicRemoteControlPool;
import com.thoughtworks.selenium.grid.hub.remotecontrol.GlobalRemoteControlPool;
import com.thoughtworks.selenium.grid.hub.remotecontrol.RemoteControlPoller;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Hub application registry.
 * <p/>
 * Provides access to global remote control pool and global environment manager.
 */
public class HubRegistry {

    private static final Log LOGGER = LogFactory.getLog(HubRegistry.class);
    private static HubRegistry singleton;
    private DynamicRemoteControlPool pool;
    private EnvironmentManager environmentManager;
    private GridConfiguration gridConfiguration;
    private LifecycleManager lifecycleManager;
    private RemoteControlPoller poller;

    public static synchronized HubRegistry registry() {
        if (null == singleton) {
            singleton = new HubRegistry();
        }
        return singleton;
    }

    public synchronized DynamicRemoteControlPool remoteControlPool() {
        if (null == pool) {
            pool = new GlobalRemoteControlPool();
        }
        return pool;
    }

    public synchronized EnvironmentManager environmentManager() {
        if (null == environmentManager) {
            environmentManager = new EnvironmentManager();
            for (EnvironmentConfiguration envConfig : gridConfiguration().getHub().getEnvironments()) {
                environmentManager.addEnvironment(new Environment(envConfig.getName(), envConfig.getBrowser()));
            }
        }
        return environmentManager;
    }


    public synchronized GridConfiguration gridConfiguration() {
        if (null == gridConfiguration) {
            final String definition = new ResourceLocator(getClass()).retrieveContent("/grid_configuration.yml");
            gridConfiguration = GridConfiguration.parse(definition);
            LOGGER.info("Loaded grid configuration:\n" + gridConfiguration.toYAML());

        }
        return gridConfiguration;
    }

    public synchronized LifecycleManager lifecycleManager() {
        if (null == lifecycleManager) {
            lifecycleManager = new LifecycleManager();
        }
        return lifecycleManager;
    }

    public synchronized RemoteControlPoller remoteControlPoller() {
        final HubConfiguration hubConfiguration;

        if (null == poller) {
            hubConfiguration = gridConfiguration().getHub();
            poller = new RemoteControlPoller(
                    remoteControlPool(),
                    hubConfiguration.getRemoteControlPollingIntervalInSeconds(),
                    hubConfiguration.getSessionMaxIdleTimeInSeconds()
            );
        }
        return poller;
    }

}
