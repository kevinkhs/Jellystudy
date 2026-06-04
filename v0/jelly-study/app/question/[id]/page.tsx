'use client'

import { useState } from 'react'
import Link from 'next/link'
import Header from '@/components/jelly-study/header'
import Footer from '@/components/jelly-study/footer'

interface Answer {
  id: number
  author: string
  content: string
  createTime: string
  likeCount: number
  isAI: boolean
}

interface Question {
  id: number
  title: string
  content: string
  author: string
  knowledgePointTitle: string
  createTime: string
  viewCount: number
  likeCount: number
  difficulty: 'easy' | 'medium' | 'hard'
  answers: Answer[]
}

const mockQuestion: Question = {
  id: 1,
  title: '如何在 React 中使用 useContext 进行状态管理？',
  content: '我想在组件间共享状态，使用 useContext 有什么最佳实践吗？我在项目中遇到了性能问题，当 Context 值变化时，所有使用该 Context 的组件都会重新渲染。有什么办法可以优化这个问题吗？',
  author: '开发小王',
  knowledgePointTitle: 'React Hooks',
  createTime: '2024-06-03 14:30',
  viewCount: 245,
  likeCount: 12,
  difficulty: 'medium',
  answers: [
    {
      id: 1,
      author: 'React 智能助手',
      content: '使用 useContext 进行状态管理时，性能优化的关键是避免不必要的重新渲染。建议的方法是：\n\n1. 将状态和 dispatch 分离，分别放在不同的 Context 中\n2. 使用 useMemo 包装 Context 的 value 值\n3. 使用 useCallback 优化回调函数\n4. 对于大型应用，考虑使用 Redux 或 Zustand 等状态管理库\n\n这样可以确保只有真正需要的组件才会重新渲染。',
      createTime: '2024-06-03 15:00',
      likeCount: 45,
      isAI: true,
    },
    {
      id: 2,
      author: '前端小李',
      content: '我的项目中就是这样解决的。把状态和 dispatch 分开确实有效果。另外还可以使用 useReducer 来管理复杂状态。',
      createTime: '2024-06-03 16:20',
      likeCount: 12,
      isAI: false,
    },
  ],
}

export default function QuestionDetailPage() {
  const [question] = useState<Question>(mockQuestion)
  const [liked, setLiked] = useState(false)
  const [showAnswerForm, setShowAnswerForm] = useState(false)

  const getDifficultyColor = (difficulty: string) => {
    switch (difficulty) {
      case 'easy':
        return 'text-success'
      case 'medium':
        return 'text-yellow-500'
      case 'hard':
        return 'text-destructive'
      default:
        return 'text-foreground'
    }
  }

  const getDifficultyText = (difficulty: string) => {
    switch (difficulty) {
      case 'easy':
        return '简单'
      case 'medium':
        return '中等'
      case 'hard':
        return '困难'
      default:
        return difficulty
    }
  }

  return (
    <div className="min-h-screen bg-background flex flex-col">
      <Header />

      <main className="flex-1">
        {/* Hero 区域 */}
        <section className="bg-gradient-primary py-8">
          <div className="container mx-auto px-4">
            <Link href="/question/list" className="text-white/70 hover:text-white transition-colors mb-4 inline-block">
              ← 返回问题列表
            </Link>
            <h1 className="text-3xl md:text-4xl font-bold text-white text-balance">{question.title}</h1>
          </div>
        </section>

        {/* 内容区域 */}
        <section className="py-12">
          <div className="container mx-auto px-4">
            <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
              {/* 主内容 */}
              <div className="lg:col-span-2">
                {/* 问题卡片 */}
                <div className="bg-card rounded-xl p-8 border border-border mb-8 shadow-lg animate-slide-up">
                  <div className="flex flex-wrap gap-3 mb-6">
                    <span className="bg-accent/10 text-accent px-4 py-1.5 rounded-full text-sm font-medium">
                      {question.knowledgePointTitle}
                    </span>
                    <span className={`${getDifficultyColor(question.difficulty)} font-bold text-sm`}>
                      难度: {getDifficultyText(question.difficulty)}
                    </span>
                  </div>

                  <div className="prose prose-invert max-w-none mb-8">
                    <p className="text-foreground whitespace-pre-wrap text-lg leading-relaxed">
                      {question.content}
                    </p>
                  </div>

                  <div className="flex flex-wrap gap-6 text-sm text-muted-foreground border-t border-border pt-6">
                    <span>👤 提问者: {question.author}</span>
                    <span>📅 提问时间: {question.createTime}</span>
                    <span>👁️ 浏览: {question.viewCount}</span>
                  </div>
                </div>

                {/* 回答列表 */}
                <div className="mb-8">
                  <h2 className="text-2xl font-bold text-foreground mb-6">
                    💬 {question.answers.length} 个回答
                  </h2>

                  <div className="space-y-6">
                    {question.answers.map((answer, index) => (
                      <div
                        key={answer.id}
                        className={`rounded-xl p-6 border ${
                          answer.isAI
                            ? 'bg-success/5 border-success/30'
                            : 'bg-card border-border'
                        } animate-slide-up hover:shadow-lg transition-shadow`}
                        style={{ animationDelay: `${index * 0.1}s` }}
                      >
                        <div className="flex justify-between items-start gap-4 mb-4">
                          <div>
                            <div className="flex items-center gap-2 mb-2">
                              <span className="font-bold text-foreground">{answer.author}</span>
                              {answer.isAI && (
                                <span className="bg-success/20 text-success px-2 py-0.5 rounded text-xs font-bold">
                                  🤖 AI 智能回答
                                </span>
                              )}
                            </div>
                            <span className="text-sm text-muted-foreground">{answer.createTime}</span>
                          </div>
                          <button className={`text-lg transition-colors ${
                            liked ? 'text-destructive' : 'text-muted-foreground hover:text-destructive'
                          }`}>
                            ❤️ {answer.likeCount}
                          </button>
                        </div>

                        <p className="text-foreground whitespace-pre-wrap leading-relaxed">
                          {answer.content}
                        </p>
                      </div>
                    ))}
                  </div>
                </div>

                {/* 回答表单 */}
                <div className="bg-card rounded-xl p-8 border border-border shadow-lg">
                  <h3 className="text-xl font-bold text-foreground mb-6">
                    {showAnswerForm ? '✍️ 写出你的回答' : '还有其他想说的？'}
                  </h3>

                  {!showAnswerForm ? (
                    <button
                      onClick={() => setShowAnswerForm(true)}
                      className="w-full bg-gradient-primary text-white font-bold py-3 px-6 rounded-xl hover:shadow-lg transition-shadow"
                    >
                      + 写出你的回答
                    </button>
                  ) : (
                    <form className="space-y-6">
                      <div>
                        <label className="block text-sm font-bold text-foreground mb-2">
                          你的昵称
                        </label>
                        <input
                          type="text"
                          placeholder="请输入你的昵称"
                          className="w-full bg-background border border-border rounded-lg px-4 py-2 text-foreground placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-primary"
                        />
                      </div>

                      <div>
                        <label className="block text-sm font-bold text-foreground mb-2">
                          你的回答
                        </label>
                        <textarea
                          placeholder="请详细描述你的想法、解决方案或建议..."
                          rows={6}
                          className="w-full bg-background border border-border rounded-lg px-4 py-2 text-foreground placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-primary resize-none"
                        />
                      </div>

                      <div className="flex gap-4">
                        <button
                          type="submit"
                          className="flex-1 bg-gradient-primary text-white font-bold py-2 px-4 rounded-lg hover:shadow-lg transition-shadow"
                        >
                          发布回答
                        </button>
                        <button
                          type="button"
                          onClick={() => setShowAnswerForm(false)}
                          className="flex-1 bg-muted text-muted-foreground font-bold py-2 px-4 rounded-lg hover:bg-muted/80 transition-colors"
                        >
                          取消
                        </button>
                      </div>
                    </form>
                  )}
                </div>
              </div>

              {/* 侧边栏 */}
              <aside className="lg:col-span-1">
                <div className="sticky top-20 space-y-6">
                  {/* 操作按钮 */}
                  <button
                    onClick={() => setLiked(!liked)}
                    className={`w-full py-3 px-6 rounded-xl font-bold transition-all ${
                      liked
                        ? 'bg-destructive text-destructive-foreground'
                        : 'bg-primary text-primary-foreground hover:shadow-lg'
                    }`}
                  >
                    {liked ? '❤️ 已赞 ' : '🤍 点赞'} {question.likeCount}
                  </button>

                  <button className="w-full bg-secondary text-secondary-foreground py-3 px-6 rounded-xl font-bold hover:shadow-lg transition-all">
                    🔖 收藏问题
                  </button>

                  <button className="w-full bg-muted text-muted-foreground py-3 px-6 rounded-xl font-bold hover:bg-muted/80 transition-colors">
                    📢 分享问题
                  </button>

                  {/* 相关知识点 */}
                  <div className="bg-card rounded-xl p-6 border border-border">
                    <h4 className="font-bold text-foreground mb-4">📚 相关知识点</h4>
                    <Link href={`/knowledge/1`} className="block text-accent hover:text-accent/80 transition-colors mb-2">
                      → {question.knowledgePointTitle}
                    </Link>
                  </div>
                </div>
              </aside>
            </div>
          </div>
        </section>
      </main>

      <Footer />
    </div>
  )
}
