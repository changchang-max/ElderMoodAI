// Mock 老人数据（10条）
export let mockElders = [
  { id: 1,  name: '张奶奶', age: 78, gender: 'female', phone: '13900001001', address: '北京市朝阳区幸福里3号', privacyAuthorized: true,  authorizedAt: '2024-02-01T10:00:00.000Z', authorizedBy: 1, guardians: [3], alertThreshold: null, createdBy: 1, createdAt: '2024-01-15T00:00:00.000Z' },
  { id: 2,  name: '李爷爷', age: 82, gender: 'male',   phone: '13900001002', address: '北京市海淀区清华园路5号', privacyAuthorized: true,  authorizedAt: '2024-02-03T09:00:00.000Z', authorizedBy: 1, guardians: [3], alertThreshold: { anxiety: 0.65, sad: 0.6, angry: 0.7 }, createdBy: 1, createdAt: '2024-01-16T00:00:00.000Z' },
  { id: 3,  name: '王奶奶', age: 75, gender: 'female', phone: '13900001003', address: '上海市浦东新区世纪大道88号', privacyAuthorized: true,  authorizedAt: '2024-02-05T11:00:00.000Z', authorizedBy: 2, guardians: [],  alertThreshold: null, createdBy: 2, createdAt: '2024-01-17T00:00:00.000Z' },
  { id: 4,  name: '陈爷爷', age: 80, gender: 'male',   phone: '13900001004', address: '广州市天河区珠江新城', privacyAuthorized: true,  authorizedAt: '2024-02-07T14:00:00.000Z', authorizedBy: 1, guardians: [],  alertThreshold: null, createdBy: 1, createdAt: '2024-01-18T00:00:00.000Z' },
  { id: 5,  name: '刘奶奶', age: 73, gender: 'female', phone: '13900001005', address: '深圳市南山区科技园路', privacyAuthorized: true,  authorizedAt: '2024-02-10T08:00:00.000Z', authorizedBy: 2, guardians: [],  alertThreshold: { anxiety: 0.8 }, createdBy: 2, createdAt: '2024-01-19T00:00:00.000Z' },
  { id: 6,  name: '赵爷爷', age: 85, gender: 'male',   phone: '13900001006', address: '成都市锦江区春熙路', privacyAuthorized: true,  authorizedAt: '2024-02-12T16:00:00.000Z', authorizedBy: 1, guardians: [],  alertThreshold: null, createdBy: 1, createdAt: '2024-01-20T00:00:00.000Z' },
  { id: 7,  name: '孙奶奶', age: 71, gender: 'female', phone: '13900001007', address: '杭州市西湖区文三路', privacyAuthorized: false, authorizedAt: null, authorizedBy: null, guardians: [], alertThreshold: null, createdBy: 2, createdAt: '2024-01-21T00:00:00.000Z' },
  { id: 8,  name: '周爷爷', age: 79, gender: 'male',   phone: '13900001008', address: '武汉市武昌区珞珈山路', privacyAuthorized: false, authorizedAt: null, authorizedBy: null, guardians: [], alertThreshold: null, createdBy: 1, createdAt: '2024-01-22T00:00:00.000Z' },
  { id: 9,  name: '吴奶奶', age: 76, gender: 'female', phone: '13900001009', address: '南京市鼓楼区中山路', privacyAuthorized: false, authorizedAt: null, authorizedBy: null, guardians: [], alertThreshold: null, createdBy: 2, createdAt: '2024-01-23T00:00:00.000Z' },
  { id: 10, name: '郑爷爷', age: 83, gender: 'male',   phone: '13900001010', address: '西安市雁塔区高新路', privacyAuthorized: false, authorizedAt: null, authorizedBy: null, guardians: [], alertThreshold: null, createdBy: 1, createdAt: '2024-01-24T00:00:00.000Z' },
]

export const genderLabels = { male: '男', female: '女', other: '其他' }

// 获取已授权老人列表
export const getAuthorizedElders = () => mockElders.filter(e => e.privacyAuthorized)
