import Link from "next/link"
import { Github, Twitter } from "lucide-react"

export default function Footer() {
  return (
    <footer className="border-t border-border/50 bg-card/30 backdrop-blur-sm">
      <div className="mx-auto max-w-7xl px-4 py-12 sm:px-6 lg:px-8">
        <div className="grid gap-8 md:grid-cols-4">
          {/* Brand */}
          <div className="space-y-4">
            <Link href="/" className="flex items-center gap-3">
              <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-gradient-to-br from-[#667eea] to-[#764ba2]">
                <span className="text-xl font-bold text-white">J</span>
              </div>
              <div>
                <span className="bg-gradient-to-r from-[#667eea] to-[#764ba2] bg-clip-text text-lg font-bold text-transparent">
                  JellyStudy
                </span>
                <span className="ml-2 text-sm text-muted-foreground">博识尊</span>
              </div>
            </Link>
            <p className="text-sm leading-relaxed text-muted-foreground">
              让知识像果冻一样清晰透明。
              <br />
              AI 驱动的智能学习平台。
            </p>
          </div>

          {/* Links */}
          <div>
            <h4 className="mb-4 font-semibold text-foreground">功能</h4>
            <ul className="space-y-2 text-sm">
              {["问题列表", "知识点管理", "AI 问答", "统计信息"].map((item) => (
                <li key={item}>
                  <Link
                    href="#"
                    className="text-muted-foreground transition-colors hover:text-[#667eea]"
                  >
                    {item}
                  </Link>
                </li>
              ))}
            </ul>
          </div>

          <div>
            <h4 className="mb-4 font-semibold text-foreground">资源</h4>
            <ul className="space-y-2 text-sm">
              {["帮助中心", "API 文档", "更新日志", "社区"].map((item) => (
                <li key={item}>
                  <Link
                    href="#"
                    className="text-muted-foreground transition-colors hover:text-[#667eea]"
                  >
                    {item}
                  </Link>
                </li>
              ))}
            </ul>
          </div>

          <div>
            <h4 className="mb-4 font-semibold text-foreground">关于</h4>
            <ul className="space-y-2 text-sm">
              {["关于我们", "隐私政策", "服务条款", "联系我们"].map((item) => (
                <li key={item}>
                  <Link
                    href="#"
                    className="text-muted-foreground transition-colors hover:text-[#667eea]"
                  >
                    {item}
                  </Link>
                </li>
              ))}
            </ul>
          </div>
        </div>

        {/* Bottom */}
        <div className="mt-10 flex flex-col items-center justify-between gap-4 border-t border-border/50 pt-8 md:flex-row">
          <p className="text-sm text-muted-foreground">
            © 2026 JellyStudy 博识尊. All rights reserved.
          </p>
          <div className="flex items-center gap-4">
            <Link
              href="#"
              className="text-muted-foreground transition-colors hover:text-[#667eea]"
            >
              <Github className="h-5 w-5" />
            </Link>
            <Link
              href="#"
              className="text-muted-foreground transition-colors hover:text-[#667eea]"
            >
              <Twitter className="h-5 w-5" />
            </Link>
          </div>
        </div>
      </div>
    </footer>
  )
}
