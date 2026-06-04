import Header from "@/components/jelly-study/header"
import HeroSection from "@/components/jelly-study/hero-section"
import StatsSection from "@/components/jelly-study/stats-section"
import QuestionsSection from "@/components/jelly-study/questions-section"
import KnowledgeSidebar from "@/components/jelly-study/knowledge-sidebar"
import AIAssistant from "@/components/jelly-study/ai-assistant"
import Footer from "@/components/jelly-study/footer"

export default function Page() {
  return (
    <div className="relative min-h-screen">
      {/* Background Grid Pattern */}
      <div className="pointer-events-none fixed inset-0 bg-[linear-gradient(to_right,#1a1a2e_1px,transparent_1px),linear-gradient(to_bottom,#1a1a2e_1px,transparent_1px)] bg-[size:4rem_4rem] [mask-image:radial-gradient(ellipse_60%_50%_at_50%_0%,#000_70%,transparent_100%)]" />
      
      {/* Content */}
      <div className="relative">
        <Header />
        <main>
          <HeroSection />
          <StatsSection />
          <QuestionsSection />
          <KnowledgeSidebar />
        </main>
        <Footer />
      </div>
      
      {/* AI Assistant Widget */}
      <AIAssistant />
    </div>
  )
}
