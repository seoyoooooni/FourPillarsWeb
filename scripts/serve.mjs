import { execFile, spawn } from "node:child_process";
import { resolve } from "node:path";

const mode = process.argv[2] === "start" ? "start" : "dev";
const root = resolve(import.meta.dirname, "..");
const next = resolve(root, "web/node_modules/.bin/next");
const server = spawn(next, [mode], {
  cwd: resolve(root, "web"),
  stdio: "inherit",
});

let opened = false;
let stopping = false;
const openWhenReady = async () => {
  for (let attempt = 0; attempt < 80 && !opened && !stopping; attempt += 1) {
    try {
      const response = await fetch("http://localhost:3000");
      if (response.ok) {
        opened = true;
        execFile("open", ["http://localhost:3000"]);
        return;
      }
    } catch {}
    await new Promise((resolveDelay) => setTimeout(resolveDelay, 250));
  }
};

openWhenReady();

for (const signal of ["SIGINT", "SIGTERM"]) {
  process.once(signal, () => {
    stopping = true;
    server.kill(signal);
    process.exit(signal === "SIGINT" ? 130 : 143);
  });
}

server.on("exit", (code) => process.exit(code ?? 0));
