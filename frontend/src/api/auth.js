/**
 * 认证相关API
 */
import axios from 'axios'

const API_BASE_URL = '/api/auth'

/**
 * 发送注册验证码
 * @param {string} email - 邮箱地址
 * @returns {Promise}
 */
export const sendVerificationCode = async (email) => {
  try {
    const response = await axios.post(`${API_BASE_URL}/send-verification-code`, { email })
    return response.data
  } catch (error) {
    throw error.response?.data || error
  }
}

/**
 * 用户注册
 * @param {Object} registerData - 注册数据
 * @param {string} registerData.username - 用户名
 * @param {string} registerData.email - 邮箱
 * @param {string} registerData.password - 密码
 * @param {string} registerData.verificationCode - 验证码
 * @returns {Promise}
 */
export const register = async (registerData) => {
  try {
    const response = await axios.post(`${API_BASE_URL}/register`, registerData)
    return response.data
  } catch (error) {
    throw error.response?.data || error
  }
}

/**
 * 用户登录（邮箱）
 * @param {Object} loginData - 登录数据
 * @param {string} loginData.email - 邮箱
 * @param {string} loginData.password - 密码
 * @returns {Promise}
 */
export const loginByEmail = async (loginData) => {
  try {
    const response = await axios.post(`${API_BASE_URL}/login/email`, loginData)
    return response.data
  } catch (error) {
    throw error.response?.data || error
  }
}

/**
 * 用户登录（手机号）
 * @param {Object} loginData - 登录数据
 * @param {string} loginData.phone - 手机号
 * @param {string} loginData.code - 验证码
 * @returns {Promise}
 */
export const loginByPhone = async (loginData) => {
  try {
    const response = await axios.post(`${API_BASE_URL}/login/phone`, loginData)
    return response.data
  } catch (error) {
    throw error.response?.data || error
  }
}

/**
 * 发送登录验证码（手机号）
 * @param {string} phone - 手机号
 * @returns {Promise}
 */
export const sendLoginCode = async (phone) => {
  try {
    const response = await axios.post(`${API_BASE_URL}/send-code`, { phone })
    return response.data
  } catch (error) {
    throw error.response?.data || error
  }
}
