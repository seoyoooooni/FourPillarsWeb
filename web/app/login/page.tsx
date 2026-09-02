import { Header } from "@/components/Header"; import { AuthForm } from "@/components/AuthForm";
export default function LoginPage() { return <main className="app-shell form-shell"><Header title="로그인" /><AuthForm mode="login" /></main>; }
