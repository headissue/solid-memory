package com.headissue.feature;

import com.headissue.Application;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import static org.junit.jupiter.api.extension.ExtensionContext.Namespace.GLOBAL;

public class ApplicationServerExtension implements BeforeAllCallback, ExtensionContext.Store.CloseableResource {

    private Thread app = new Thread(() -> Application.main(null));

    public Thread getApp() {
        return app;
    }

    @Override
    public void beforeAll(ExtensionContext context) {
        ExtensionContext.Store store = context.getRoot().getStore(GLOBAL);
        ApplicationServerExtension extension = (ApplicationServerExtension) store.get("application server");
        if (extension == null) {
            app.start();
            store.put("application server", this);
        } else {
            app = extension.getApp();
        }
    }

    @Override
    public void close() {
        app.interrupt();
    }
}