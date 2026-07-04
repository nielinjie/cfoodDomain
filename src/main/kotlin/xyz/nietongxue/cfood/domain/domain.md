"我将描述一些需求，你帮我记录整理，形成领..."点击查看元宝的回答
https://yb.tencent.com/s/qvMF4eIWkcsN



领域模型 v2（核心实体）

1️⃣ 生产工单域（Order Domain）

ProductionOrder（生产工单）
id
orderNo
planStartDate / planEndDate
status：待排产 / 已排产 / 下达 / 完工
items：List<OrderItem>
OrderItem（工单行）
id
product
quantity
demandDate
plannedOperations：List<OrderOperation>
2️⃣ 产品与 BOM 域（Product / BOM Domain）

Product（产品/物料）
id
code
name
productType：成品 / 半成品 / 原材料
produceType：自制 / 外协 / 采购
BOM（物料清单）
id
product
version
status：生效 / 失效
items：List<BOMItem>
BOMItem（BOM子项）
id
componentProduct
quantity
unit
scrapRate（可选）
isCritical
⚠️ BOM 是递归结构，支持多层展开。

3️⃣ 工艺与工位域（Routing / Station Domain）

Routing（工艺路线）
id
product
version
status
operations：List<RoutingOperation>
RoutingOperation（工艺工序定义）
operationNo
name
sequence
standardTime
station：Station
Station（工位）
id
code
name
capacityPerDay
workCenter（可选）
4️⃣ 生产计划 & 派工域（Planning / Dispatch Domain）

OrderOperation（工单工序任务）
id
orderItem
routingOperation（来源工序定义）
station
plannedQty
plannedStartTime / endTime
actualStartTime / endTime
status：未开始 / 加工中 / 完成 / 异常
✅ 这是计划与执行的核心对象

三、核心关系图（简化文字版）
纯文本
ProductionOrder
└── OrderItem
├── Product
│    ├── BOM
│    │    └── BOMItem → Product（递归）
│    └── Routing
│         └── RoutingOperation → Station
└── OrderOperation（由 RoutingOperation 生成）
四、关键领域行为（v2 新增）

✅ BOM 多层展开（领域服务）
纯文本
expandBOM(product, quantity)
→ List<BOMNode>
→ 每个节点包含：product / level / totalQty
✅ 工单工序生成（领域服务）
纯文本
generateOrderOperations(orderItem)
→ 遍历 product.routing.operations
→ 创建 OrderOperation
→ 绑定 station
✅ 工序派工（调度）
纯文本
dispatch(orderOperation, station, timeSlot)
→ 占用工位产能
→ 更新计划时间


Bill of Materials（物料清单）

