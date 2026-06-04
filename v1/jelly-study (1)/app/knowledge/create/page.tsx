'use client'

import { useState } from 'react'
import Link from 'next/link'
import Header from '@/components/jelly-study/header'
import Footer from '@/components/jelly-study/footer'

const mockCategories = ['Frontend', 'Backend', 'Language', 'Database', 'DevOps', 'Other']

export default function CreateKnowledgePage() {
  const [formData, setFormData] = useState({
    title: '',
    category: '',
    description: '',
    content: '',
  })

  const [errors, setErrors] = useState<Record<string, string>>({})
  const [submitted, setSubmitted] = useState(false)

  const validateForm = () => {
    const newErrors: Record<string, string> = {}

    if (!formData.title.trim()) {
      newErrors.title = '标题不能为空'
    } else if (formData.title.length < 3) {
      newErrors.title = '标题至少需要 3 个字符'
    } else if (formData.title.length > 100) {
      newErrors.title = '标题不能超过 100 个字符'
    }

    if (!formData.category) {
      newErrors.category = '请选择分类'
    }

    if (!formData.description.trim()) {
      newErrors.description = '描述不能为空'
    } else if (formData.description.length < 20) {
      newErrors.description = '描述至少需要 20 个字符'
    } else if (formData.description.length > 500) {
      newErrors.description = '描述不能超过 500 个字符'
    }

    if (!formData.content.trim()) {
      newErrors.content = '内容不能为空'
    } else if (formData.content.length < 100) {
      newErrors.content = '内容至少需要 100 个字符'
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

    setSubmitted(true)
    console.log('Form submitted:', formData)

    setTimeout(() => {
      setSubmitted(false)
      setFormData({
        title: '',
        category: '',
        description: '',
        content: '',
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
              <h1 className="text-4xl font-bold text-white mb-4">创建知识点</h1>
              <p className="text-lg text-white/80">分享你的知识和经验，帮助更多的学习者成长</p>
            </div>
          </div>
        </section>

        {/* 表单区域 */}
        <section className="py-12">
          <div className="container mx-auto px-4">
            <div className="max-w-3xl mx-auto">
              {/* 提示信息 */}
              <div className="bg-accent/10 border border-accent/30 rounded-xl p-6 mb-8 animate-slide-up">
                <h3 className="font-bold text-accent mb-3">💡 创建建议</h3>
                <ul className="text-sm text-muted-foreground space-y-2">
                  <li>✓ 选择一个明确的知识点主题</li>
                  <li>✓ 提供清晰详尽的描述</li>
                  <li>✓ 包含实际的代码示例和最佳实践</li>
                  <li>✓ 标记相关的知识点和主题</li>
                </ul>
              </div>

              {/* 成功提示 */}
              {submitted && (
                <div className="bg-success/10 border border-success/30 rounded-xl p-6 mb-8 animate-slide-up">
                  <p className="text-success font-bold">✓ 知识点创建成功！感谢你的贡献。</p>
                </div>
              )}

              {/* 表单 */}
              <form onSubmit={handleSubmit} className="animate-slide-up">
                {/* 标题 */}
                <div className="mb-8">
                  <label className="block text-lg font-bold text-foreground mb-3">
                    知识点标题 *
                  </label>
                  <input
                    type="text"
                    name="title"
                    value={formData.title}
                    onChange={handleChange}
                    placeholder="例如：React Hooks 深入理解"
                    maxLength={100}
                    className={`w-full bg-card border-2 rounded-lg px-4 py-3 text-foreground placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-primary transition-all ${
                      errors.title ? 'border-destructive' : 'border-border'
                    }`}
                  />
                  <div className="flex justify-between items-center mt-2">
                    <p className="text-sm text-muted-foreground">{formData.title.length}/100</p>
                    {errors.title && (
                      <p className="text-sm text-destructive">{errors.title}</p>
                    )}
                  </div>
                </div>

                {/* 分类选择 */}
                <div className="mb-8">
                  <label className="block text-lg font-bold text-foreground mb-3">
                    知识点分类 *
                  </label>
                  <select
                    name="category"
                    value={formData.category}
                    onChange={handleChange}
                    className={`w-full bg-card border-2 rounded-lg px-4 py-3 text-foreground focus:outline-none focus:ring-2 focus:ring-primary transition-all ${
                      errors.category ? 'border-destructive' : 'border-border'
                    }`}
                  >
                    <option value="">-- 请选择分类 --</option>
                    {mockCategories.map(cat => (
                      <option key={cat} value={cat}>
                        {cat}
                      </option>
                    ))}
                  </select>
                  {errors.category && (
                    <p className="text-sm text-destructive mt-2">{errors.category}</p>
                  )}
                </div>

                {/* 描述 */}
                <div className="mb-8">
                  <label className="block text-lg font-bold text-foreground mb-3">
                    知识点描述 *
                  </label>
                  <textarea
                    name="description"
                    value={formData.description}
                    onChange={handleChange}
                    placeholder="简要描述这个知识点的核心内容和重要性"
                    rows={4}
                    maxLength={500}
                    className={`w-full bg-card border-2 rounded-lg px-4 py-3 text-foreground placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-primary resize-none transition-all ${
                      errors.description ? 'border-destructive' : 'border-border'
                    }`}
                  />
                  <div className="flex justify-between items-center mt-2">
                    <p className="text-sm text-muted-foreground">{formData.description.length}/500</p>
                    {errors.description && (
                      <p className="text-sm text-destructive">{errors.description}</p>
                    )}
                  </div>
                </div>

                {/* 详细内容 */}
                <div className="mb-8">
                  <label className="block text-lg font-bold text-foreground mb-3">
                    详细内容 *
                  </label>
                  <textarea
                    name="content"
                    value={formData.content}
                    onChange={handleChange}
                    placeholder="详细讲解这个知识点，包括概念、使用方法、最佳实践、代码示例等&#10;支持 Markdown 格式&#10;&#10;示例：&#10;## 标题&#10;### 小标题&#10;- 列表项&#10;`代码`"
                    rows={12}
                    className={`w-full bg-card border-2 rounded-lg px-4 py-3 text-foreground placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-primary resize-none font-mono text-sm transition-all ${
                      errors.content ? 'border-destructive' : 'border-border'
                    }`}
                  />
                  {errors.content && (
                    <p className="text-sm text-destructive mt-2">{errors.content}</p>
                  )}
                </div>

                {/* 按钮 */}
                <div className="flex gap-4">
                  <button
                    type="submit"
                    disabled={submitted}
                    className="flex-1 bg-gradient-primary text-white font-bold py-3 px-6 rounded-xl hover:shadow-lg transition-all disabled:opacity-50 disabled:cursor-not-allowed text-lg"
                  >
                    {submitted ? '✓ 创建成功' : '✓ 创建知识点'}
                  </button>
                  <Link
                    href="/knowledge/list"
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
