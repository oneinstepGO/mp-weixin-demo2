package com.oneinstep.demo.sharding.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.oneinstep.demo.sharding.domain.EventLog;
import com.oneinstep.demo.sharding.mapper.EventLogMapper;
import com.oneinstep.demo.sharding.service.EventLogService;
import org.springframework.stereotype.Service;

@Service
public class EventLogServiceImpl extends ServiceImpl<EventLogMapper, EventLog> implements EventLogService {
}