import * as path from "path";
import { ExtensionContext } from "vscode";
import {
  LanguageClient,
  LanguageClientOptions,
  ServerOptions,
  TransportKind,
} from "vscode-languageclient/node";

let client: LanguageClient | undefined;

export function activate(context: ExtensionContext): void {
  const serverPath =
    context.asAbsolutePath(path.join("..", "..", "build", "libs", "hasab-lang-1.0.0-all.jar"));

  const serverOptions: ServerOptions = {
    run: {
      command: "java",
      args: ["-jar", serverPath],
      options: { env: { ...process.env } },
    },
    debug: {
      command: "java",
      args: ["-jar", serverPath],
      options: { env: { ...process.env } },
    },
  };

  const clientOptions: LanguageClientOptions = {
    documentSelector: [{ scheme: "file", language: "hasab" }],
    synchronize: {
      fileEvents: context.workspace.createFileSystemWatcher("**/*.hs"),
    },
  };

  client = new LanguageClient(
    "hasabLangServer",
    "HASAB Language Server",
    serverOptions,
    clientOptions
  );

  client.start();

  context.subscriptions.push({
    dispose: () => {
      if (client) {
        client.stop();
      }
    },
  });
}

export function deactivate(): Thenable<void> | undefined {
  if (!client) {
    return undefined;
  }
  return client.stop();
}
