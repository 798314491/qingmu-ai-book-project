<template>
  <div class="captcha-container">
    <canvas
      ref="canvasRef"
      :width="width"
      :height="height"
      @click="refresh"
      class="cursor-pointer border border-gray-300 rounded"
      title="点击刷新验证码"
    ></canvas>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'

interface Props {
  width?: number
  height?: number
  length?: number
}

const props = withDefaults(defineProps<Props>(), {
  width: 100,
  height: 40,
  length: 4
})

const emit = defineEmits<{
  change: [value: string]
}>()

const canvasRef = ref<HTMLCanvasElement>()
const captchaCode = ref('')

// 生成随机字符
const generateCode = () => {
  const chars = '0123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijklmnpqrstuvwxyz'
  let code = ''
  for (let i = 0; i < props.length; i++) {
    code += chars[Math.floor(Math.random() * chars.length)]
  }
  return code
}

// 生成随机颜色
const getRandomColor = () => {
  const colors = ['#FF6B6B', '#4ECDC4', '#45B7D1', '#96CEB4', '#FFEAA7', '#DDA0DD', '#98D8C8']
  return colors[Math.floor(Math.random() * colors.length)]
}

// 绘制验证码
const drawCaptcha = () => {
  const canvas = canvasRef.value
  if (!canvas) return

  const ctx = canvas.getContext('2d')
  if (!ctx) return

  // 清空画布
  ctx.clearRect(0, 0, props.width, props.height)

  // 设置背景色
  ctx.fillStyle = '#f8f9fa'
  ctx.fillRect(0, 0, props.width, props.height)

  // 生成验证码
  captchaCode.value = generateCode()

  // 绘制文字
  ctx.font = 'bold 20px Arial'
  ctx.textBaseline = 'middle'
  
  const charWidth = props.width / props.length
  
  for (let i = 0; i < captchaCode.value.length; i++) {
    ctx.fillStyle = getRandomColor()
    const x = charWidth * i + charWidth / 2
    const y = props.height / 2 + (Math.random() - 0.5) * 6
    const angle = (Math.random() - 0.5) * 0.4
    
    ctx.save()
    ctx.translate(x, y)
    ctx.rotate(angle)
    ctx.fillText(captchaCode.value[i], -6, 0)
    ctx.restore()
  }

  // 绘制干扰线
  for (let i = 0; i < 3; i++) {
    ctx.strokeStyle = getRandomColor()
    ctx.lineWidth = 1
    ctx.beginPath()
    ctx.moveTo(Math.random() * props.width, Math.random() * props.height)
    ctx.lineTo(Math.random() * props.width, Math.random() * props.height)
    ctx.stroke()
  }

  // 绘制干扰点
  for (let i = 0; i < 20; i++) {
    ctx.fillStyle = getRandomColor()
    ctx.beginPath()
    ctx.arc(Math.random() * props.width, Math.random() * props.height, 1, 0, 2 * Math.PI)
    ctx.fill()
  }

  // 发送验证码值给父组件
  emit('change', captchaCode.value)
}

// 刷新验证码
const refresh = () => {
  drawCaptcha()
}

// 获取验证码值
const getCode = () => {
  return captchaCode.value
}

// 验证输入
const verify = (input: string) => {
  return input.toLowerCase() === captchaCode.value.toLowerCase()
}

// 暴露方法给父组件
defineExpose({
  refresh,
  getCode,
  verify
})

onMounted(() => {
  drawCaptcha()
})

watch(() => [props.width, props.height, props.length], () => {
  drawCaptcha()
})
</script>

<style scoped>
.captcha-container {
  display: inline-block;
}
</style>
