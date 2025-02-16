#!/bin/bash

# 测试创建条件单
echo "Testing create conditional order..."
curl -X POST http://localhost:9090/api/conditional-orders \
  -H "Content-Type: application/json" \
  -d '{
    "stockCode": "000001",
    "costPrice": 50.0,
    "quantity": 100,
    "takeProfitRate": 0.5,
    "stopLossRate": 0.2
  }'

echo -e "\n\n"

# 测试止盈触发场景
echo "Testing take profit scenario..."
curl -X POST http://localhost:9090/api/conditional-orders/check \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "CO001",
    "stockCode": "000001",
    "costPrice": 50.0,
    "currentPrice": 75.0,
    "quantity": 100,
    "takeProfitRate": 0.5,
    "stopLossRate": 0.2,
    "status": "PENDING"
  }'

echo -e "\n\n"

# 测试止损触发场景
echo "Testing stop loss scenario..."
curl -X POST http://localhost:9090/api/conditional-orders/check \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "CO002",
    "stockCode": "000001",
    "costPrice": 50.0,
    "currentPrice": 39.0,
    "quantity": 100,
    "takeProfitRate": 0.5,
    "stopLossRate": 0.2,
    "status": "PENDING"
  }'

echo -e "\n\n"

# 测试正常持仓场景
echo "Testing normal holding scenario..."
curl -X POST http://localhost:9090/api/conditional-orders/check \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "CO003",
    "stockCode": "000001",
    "costPrice": 50.0,
    "currentPrice": 55.0,
    "quantity": 100,
    "takeProfitRate": 0.5,
    "stopLossRate": 0.2,
    "status": "PENDING"
  }' 