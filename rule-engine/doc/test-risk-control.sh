#!/bin/bash

# 测试普通账户大额交易
echo "Testing large amount trade for normal account..."
curl -X POST http://localhost:9090/api/risk-control/check \
  -H "Content-Type: application/json" \
  -d '{
    "order": {
      "orderId": "O001",
      "accountId": "A001",
      "stockCode": "000001",
      "type": "BUY",
      "price": 50.0,
      "quantity": 10000
    },
    "account": {
      "id": "A001",
      "level": "普通账户",
      "balance": 1000000.0,
      "dailyTradeCount": 10,
      "stockPositions": {
        "000001": 0.1
      }
    }
  }'

echo -e "\n\n"

# 测试频繁交易
echo "Testing frequent trading..."
curl -X POST http://localhost:9090/api/risk-control/check \
  -H "Content-Type: application/json" \
  -d '{
    "order": {
      "orderId": "O002",
      "accountId": "A002",
      "stockCode": "000001",
      "type": "BUY",
      "price": 50.0,
      "quantity": 100
    },
    "account": {
      "id": "A002",
      "level": "普通账户",
      "balance": 1000000.0,
      "dailyTradeCount": 51,
      "stockPositions": {
        "000001": 0.1
      }
    }
  }' 