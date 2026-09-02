import { Header } from "@/components/Header"; import { AuthForm } from "@/components/AuthForm";
export default function SignupPage() { return <main className="app-shell form-shell"><Header title="회원가입" /><AuthForm mode="signup" /></main>; }
