'use client'

import { useState } from 'react'
import Link from 'next/link'
import Header from '@/components/jelly-study/header'
import Footer from '@/components/jelly-study/footer'

const mockKnowledgePoints = [
  { id: 1, title: 'React Hooks', category: 'Frontend' },
  { id: 2, title: 'Next.js', category: 'Frontend' },
  { id: 3, title: 'TypeScript', category: 'Language' },
  { id: 4, title: 'Node.js', category: 'Backend' },
  { id: 5, title: 'Database Design', category: 'Backend' },
]

export default function CreateQuestionPage() {
  const [formData, setFormData] = useState({
    title: '',
    content: '',
    knowledgePointId: '',
    author: '',
  })

  const [errors, setErrors] = useState<Record<string, string>>({})
  const [submitted, setSubmitted] = useState(false)

  const validateForm = () => {
    const newErrors: Record<string, string> = {}

    if (!formData.title.trim()) {
      newErrors.title = '问题标题不能为空'
    } else if (formData.title.length < 5) {
      newErrors.title = '问题标题至少需要 5 个字符'
    } else if (formData.title.length > 200) {
      newErrors.title = '问题标题不能超过 200 个字符'
    }

    if (!formData.content.trim()) {
      newErrors.content = '问题详情不能为空'
    } else if (formData.content.length < 20) {
      newErrors.content = '问题详情至少需要 20 个字符'
    }

    if (!formData.knowledgePointId) {
      newErrors.knowledgePointId = '请选择相关的知识点'
    }

    if (!formData.author.trim()) {
      newErrors.author = '昵称不能为空'
    } else if (formData.author.length > 50) {
      newErrors.author = '昵称不能超过 50 个字符'
    }

    setErrors(newErrors)
    return Object.keys(newErrors).length === 0
  }

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
    const { name, value } = e.target
    setFormData(prev => ({
      ...prev,
      [name]: value,
    }))
    // 清除该字段的错误信息
    if (errors[name]) {
      setErrors(prev => ({
        ...prev,
        [name]: '',
      }))
    }
  }

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()

    if (!validateForm()) {
      return
    }

    // 模拟提交
    setSubmitted(true)
    console.log('Form submitted:', formData)

    // 2 秒后重置
    setTimeout(() => {
      setSubmitted(false)
      setFormData({
        title: '',
        content: '',
        knowledgePointId: '',
        author: '',
      })
    }, 2000)
  }

  return (
    <div className="min-h-screen bg-background flex flex-col">
      <Header />

      <main className="flex-1">
        {/* Hero 区域 */}
        <section className="bg-gradient-primary py-12">
          <div className="container mx-auto px-4">
            <div className="animate-slide-up">
              <h1 className="text-4xl font-bold text-white mb-4">提出新问题</h1>
              <p className="text-lg text-white/80">分享你的疑惑，获得社区的帮助 - 清晰的问题描述能获得更好的回答</p>
            </div>
          </div>
        </section>

        {/* 表单区域 */}
        <section className="py-12">
          <div className="container mx-auto px-4">
            <div className="max-w-3xl mx-auto">
              {/* 提示信息 */}
              <div className="bg-accent/10 border border-accent/30 rounded-xl p-6 mb-8 animate-slide-up">
                <h3 className="font-bold text-accent mb-3">💡 提问建议</h3>
                <ul className="text-sm text-muted-foreground space-y-2">
                  <li>✓ 使用清晰、简洁的语言描述你的问题</li>
                  <li>✓ 提供足够的背景信息和代码示例</li>
                  <li>✓ 描述你已经尝试过的方法</li>
                  <li>✓ 包含任何错误信息或意外行为</li>
                </ul>
              </div>

              {/* 成功提示 */}
              {submitted && (
                <div className="bg-success/10 border border-success/30 rounded-xl p-6 mb-8 animate-slide-up">
                  <p className="text-success font-bold">✓ 问题发布成功！感谢你的贡献。</p>
                </div>
              )}

              {/* 表单 */}
              <form onSubmit={handleSubmit} className="animate-slide-up">
                {/* 问题标题 */}
                <div className="mb-8">
                  <label className="block text-lg font-bold text-foreground mb-3">
                    问题标题 *
                  </label>
                  <input
                    type="text"
                    name="title"
                    value={formData.title}
                    onChange={handleChange}
                    placeholder="用一句话总结你的问题，例如：如何在 React 中优化性能？"
                    maxLength={200}
                    className={`w-full bg-card border-2 rounded-lg px-4 py-3 text-foreground placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-primary transition-all ${
                      errors.title ? 'border-destructive' : 'border-border'
                    }`}
                  />
                  <div className="flex justify-between items-center mt-2">
                    <p className="text-sm text-muted-foreground">{formData.title.length}/200</p>
                    {errors.title && (
                      <p className="text-sm text-destructive">{errors.title}</p>
                    )}
                  </div>
                </div>

                {/* 问题详情 */}
                <div className="mb-8">
                  <label className="block text-lg font-bold text-foreground mb-3">
                    问题详情 *
                  </label>
                  <textarea
                    name="content"
                    value={formData.content}
                    onChange={handleChange}
                    placeholder="详细描述你的问题，包括：&#10;- 你想要达到什么目的&#10;- 目前遇到了什么问题&#10;- 你已经尝试过的方法&#10;- 任何相关的代码片段"
                    rows={8}
                    className={`w-full bg-card border-2 rounded-lg px-4 py-3 text-foreground placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-primary resize-none transition-all ${
                      errors.content ? 'border-destructive' : 'border-border'
                    }`}
                  />
                  {errors.content && (
                    <p className="text-sm text-destructive mt-2">{errors.content}</p>
                  )}
                </div>

                {/* 知识点选择 */}
                <div className="mb-8">
                  <label className="block text-lg font-bold text-foreground mb-3">
                    选择相关知识点 *
                  </label>
                  <select
                    name="knowledgePointId"
                    value={formData.knowledgePointId}
                    onChange={handleChange}
                    className={`w-full bg-card border-2 rounded-lg px-4 py-3 text-foreground focus:outline-none focus:ring-2 focus:ring-primary transition-all ${
                      errors.knowledgePointId ? 'border-destructive' : 'border-border'
                    }`}
                  >
                    <option value="">-- 请选择知识点 --</option>
                    {mockKnowledgePoints.map(kp => (
                      <option key={kp.id} value={kp.id}>
                        {kp.title} ({kp.category})
                      </option>
                    ))}
                  </select>
                  {errors.knowledgePointId && (
                    <p className="text-sm text-destructive mt-2">{errors.knowledgePointId}</p>
                  )}
                </div>

                {/* 昵称 */}
                <div className="mb-8">
                  <label className="block text-lg font-bold text-foreground mb-3">
                    你的昵称 *
                  </label>
                  <input
                    type="text"
                    name="author"
                    value={formData.author}
                    onChange={handleChange}
                    placeholder="请输入你的昵称（不能超过 50 个字符）"
                    maxLength={50}
                    className={`w-full bg-card border-2 rounded-lg px-4 py-3 text-foreground placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-primary transition-all ${
                      errors.author ? 'border-destructive' : 'border-border'
                    }`}
                  />
                  {errors.author && (
                    <p className="text-sm text-destructive mt-2">{errors.author}</p>
                  )}
                </div>

                {/* 按钮 */}
                <div className="flex gap-4">
                  <button
                    type="submit"
                    disabled={submitted}
                    className="flex-1 bg-gradient-primary text-white font-bold py-3 px-6 rounded-xl hover:shadow-lg transition-all disabled:opacity-50 disabled:cursor-not-allowed text-lg"
                  >
                    {submitted ? '✓ 发布成功' : '✓ 发布问题'}
                  </button>
                  <Link
                    href="/question/list"
                    className="flex-1 bg-muted text-muted-foreground font-bold py-3 px-6 rounded-xl hover:bg-muted/80 transition-colors text-center text-lg"
                  >
                    取消
                  </Link>
                </div>
              </form>
            </div>
          </div>
        </section>
      </main>

      <Footer />
    </div>
  )
}
