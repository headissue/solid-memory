package com.headissue.feature;

import com.headissue.Application;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import static org.junit.jupiter.api.extension.ExtensionContext.Namespace.GLOBAL;

public class ApplicationServerExtension implements BeforeAllCallback, ExtensionContext.Store.CloseableResource {

    final Thread app = new Thread(() -> Application.main(null));

    @Override
    public void beforeAll(ExtensionContext context) {
        app.start();
        context.getRoot().getStore(GLOBAL).put("application server", this);
    }

    @Override
    public void close() {
        app.interrupt();
    }
}