import { NextRequest } from "next/server";

export const dynamic = "force-dynamic";
export const runtime = "nodejs";

const requestHeaders = ["accept", "authorization", "content-type"];
const responseHeaders = ["cache-control", "content-type", "location"];

async function proxy(request: NextRequest, context: { params: Promise<{ path: string[] }> }) {
  const backendUrl = process.env.BACKEND_URL ?? "http://127.0.0.1:8080";
  const { path } = await context.params;
  const target = new URL(`${backendUrl.replace(/\/$/, "")}/${path.map(encodeURIComponent).join("/")}`);
  target.search = request.nextUrl.search;

  const headers = new Headers();
  for (const name of requestHeaders) {
    const value = request.headers.get(name);
    if (value) headers.set(name, value);
  }

  const hasBody = request.method !== "GET" && request.method !== "HEAD";
  const response = await fetch(target, {
    method: request.method,
    headers,
    body: hasBody ? await request.arrayBuffer() : undefined,
    cache: "no-store",
    redirect: "manual",
  });

  const outgoingHeaders = new Headers();
  for (const name of responseHeaders) {
    const value = response.headers.get(name);
    if (value) outgoingHeaders.set(name, value);
  }

  return new Response(response.body, {
    status: response.status,
    headers: outgoingHeaders,
  });
}

export const GET = proxy;
export const POST = proxy;
export const PUT = proxy;
export const PATCH = proxy;
export const DELETE = proxy;
export const OPTIONS = proxy;
