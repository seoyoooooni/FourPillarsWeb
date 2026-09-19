import { BackButton } from "@/components/BackButton";

export function Header({ title: _title }: { title?: string }) {
  return <header className="page-header"><BackButton /></header>;
}
