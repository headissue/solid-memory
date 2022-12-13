package com.headissue.feature;

import static org.junit.jupiter.api.extension.ExtensionContext.Namespace.GLOBAL;

import com.headissue.Application;
import java.io.IOException;
import java.net.URISyntaxException;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class ApplicationServerExtension
    implements BeforeAllCallback, ExtensionContext.Store.CloseableResource {

  private Thread app =
      new Thread(
          () -> {
            try {
              Application.main(null);
            } catch (IOException | URISyntaxException e) {
              throw new RuntimeException(e);
            }
          });

  public Thread getApp() {
    return app;
  }

  @Override
  public void beforeAll(ExtensionContext context) {
    ExtensionContext.Store store = context.getRoot().getStore(GLOBAL);
    ApplicationServerExtension extension =
        (ApplicationServerExtension) store.get("application server");
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
