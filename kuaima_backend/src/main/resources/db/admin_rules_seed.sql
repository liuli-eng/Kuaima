-- 规则管理基础数据：信用分规则、飞单认定与处理
-- 可重复执行：仅在同名规则不存在时插入，不覆盖已有规则内容。

INSERT INTO admin_rules (title, category, version, status, content, create_time, update_time)
SELECT '信用分总则', '信用分规则', 'v2.0', 'published',
'一、目的
建立诚信交易环境，保障老板和零工双方权益。

二、基础分
平台用户初始信用分为100分，根据平台内实际行为进行加分或扣分。

三、等级划分
优秀：95分以上
良好：85-94分
普通：70-84分
较差：60-69分
不良：60分以下

四、影响范围
信用分将影响报名权限、接单优先级、平台服务费率及部分平台权益。'
, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM admin_rules WHERE title = '信用分总则');

INSERT INTO admin_rules (title, category, version, status, content, create_time, update_time)
SELECT '零工信用分规则', '信用分规则', 'v1.8', 'published',
'一、加分项
1. 完成订单：+2分/单
2. 获得老板好评：+1分/次
3. 连续完成10单：+5分

二、减分项
1. 无故爽约：-5分/次
2. 被老板投诉：-3分/次
3. 迟到或早退：-2分/次
4. 服务态度差：-2分/次

三、限制措施
信用分低于60分的零工将被限制报名或接单。'
, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM admin_rules WHERE title = '零工信用分规则');

INSERT INTO admin_rules (title, category, version, status, content, create_time, update_time)
SELECT '老板信用分规则', '信用分规则', 'v1.6', 'published',
'一、加分项
1. 按时结算：+3分/次
2. 获得零工好评：+1分/次

二、减分项
1. 超时结算：-5分/次
2. 拖欠工资：-10分/次
3. 发布虚假招工：-10分/次
4. 被投诉：-3分/次

三、限制措施
信用分过低的老板将被限制发布招工或使用部分平台服务。'
, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM admin_rules WHERE title = '老板信用分规则');

INSERT INTO admin_rules (title, category, version, status, content, create_time, update_time)
SELECT '信用分恢复机制', '信用分规则', 'v1.2', 'published',
'信用分恢复方式：
1. 自然恢复：无违规行为时，每月恢复2分
2. 活跃恢复：每月完成5单以上恢复3分
3. 好评恢复：获得老板好评后额外恢复信用分
4. 恢复上限：每月最多恢复5分

恢复后的信用分不得超过平台规定的基础分上限。'
, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM admin_rules WHERE title = '信用分恢复机制');

INSERT INTO admin_rules (title, category, version, status, content, create_time, update_time)
SELECT '信用分等级权益', '信用分规则', 'v1.0', 'draft',
'不同信用分等级享受不同平台权益：

优秀（95分以上）：优先接单、低费率、专属客服
良好（85-94分）：正常接单、标准费率
普通（70-84分）：限制接单数量、较高费率
较差（60-69分）：需提升信用分后恢复完整权限
不良（60分以下）：冻结相关账号功能，完成申诉或整改后复核。'
, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM admin_rules WHERE title = '信用分等级权益');

INSERT INTO admin_rules (title, category, version, status, content, create_time, update_time)
SELECT '信用分申诉机制', '信用分规则', 'v1.0', 'archived',
'信用分申诉流程：
1. 在平台提交申诉申请
2. 说明申诉理由并上传相关凭证
3. 平台在规定工作日内完成审核
4. 审核通过后修正信用分，审核不通过则维持原结果
5. 对处理结果仍有异议，可按平台规则申请再次复核。'
, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM admin_rules WHERE title = '信用分申诉机制');

INSERT INTO admin_rules (title, category, version, status, content, create_time, update_time)
SELECT '飞单认定标准', '飞单认定与处理规则', 'v1.8', 'published',
'以下情况视为飞单（绕过平台进行私下交易）：

一、绕过平台交易
1. 双方通过平台获取联系方式后私下交易
2. 使用平台招工信息但不通过平台下单

二、提供虚假信息
1. 虚构用工需求
2. 虚报工作时长或结算金额
3. 伪造结算凭证

三、诱导私下交易
1. 承诺平台外结算
2. 提供额外优惠诱导脱离平台
3. 以其他方式规避平台监管

四、其他情形
双方直接签订合同、直接转账且不通过平台完成交易的，均可纳入飞单认定范围。'
, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM admin_rules WHERE title = '飞单认定标准');

INSERT INTO admin_rules (title, category, version, status, content, create_time, update_time)
SELECT '飞单举证要求', '飞单认定与处理规则', 'v1.5', 'published',
'飞单举证材料包括：

一、聊天记录截图
1. 包含交易意向
2. 包含联系方式
3. 包含交易细节

二、转账记录截图
1. 银行或支付平台截图
2. 需显示对方信息
3. 需显示转账金额

三、现场照片
1. 工作场所照片
2. 双方合影或工作状态照片

四、其他凭证
合同或协议、收款凭证、证人证言等能够证明私下交易事实的材料。'
, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM admin_rules WHERE title = '飞单举证要求');

INSERT INTO admin_rules (title, category, version, status, content, create_time, update_time)
SELECT '飞单处罚规则', '飞单认定与处理规则', 'v1.6', 'published',
'一、零工端处罚
1. 首次违规：警告并扣10信用分
2. 二次违规：扣20信用分并冻结账号7天
3. 三次违规：永久封禁账号

二、老板端处罚
1. 首次违规：警告并扣10信用分
2. 二次违规：扣20信用分并冻结账号14天
3. 三次违规：永久封禁账号并列入平台黑名单

三、赔偿处理
1. 按飞单金额的20%收取违约金
2. 赔偿平台服务费损失
3. 对造成的其他实际损失依法依规处理。'
, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM admin_rules WHERE title = '飞单处罚规则');

INSERT INTO admin_rules (title, category, version, status, content, create_time, update_time)
SELECT '飞单申诉流程', '飞单认定与处理规则', 'v1.0', 'published',
'飞单申诉流程：
1. 收到处罚通知后，在7天内提交申诉
2. 提交申诉理由和相关证据
3. 平台在3个工作日内完成审核并回复
4. 申诉成功的，撤销对应处罚并恢复相关权益
5. 申诉失败的，维持原认定和处罚结果
6. 对处理结果仍有异议，可按平台规定向更高层级申请复核。'
, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM admin_rules WHERE title = '飞单申诉流程');
