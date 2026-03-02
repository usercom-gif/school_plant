import React from 'react';
import ReactECharts from 'echarts-for-react';
import { Card, Row, Col, Statistic, Button } from 'antd';
import { DownloadOutlined, TrophyOutlined } from '@ant-design/icons';

const MyAchievements: React.FC = () => {
  // Chart 1: Task Completion Rate
  const gaugeOption = {
    tooltip: { formatter: '{a} <br/>{b} : {c}%' },
    series: [
      {
        name: '任务完成率',
        type: 'gauge',
        progress: { show: true },
        detail: { valueAnimation: true, formatter: '{value}' },
        data: [{ value: 85, name: '完成率' }]
      }
    ]
  };

  // Chart 2: Monthly Activity
  const barOption = {
    title: { text: '每月养护次数' },
    tooltip: {},
    xAxis: { data: ['1月', '2月', '3月', '4月', '5月', '6月'] },
    yAxis: {},
    series: [{ name: '次数', type: 'bar', data: [5, 20, 36, 10, 10, 20] }]
  };

  return (
    <div>
      <div className="flex justify-between items-center mb-6">
        <h2 className="text-xl font-bold">我的认养成果</h2>
        <Button type="primary" icon={<DownloadOutlined />}>下载电子证书</Button>
      </div>

      <Row gutter={[16, 16]} className="mb-8">
        <Col span={8}>
          <Card>
            <Statistic title="累计认养时长" value={365} suffix="天" prefix={<ClockCircleOutlined />} />
          </Card>
        </Col>
        <Col span={8}>
          <Card>
            <Statistic title="获得勋章" value={3} prefix={<TrophyOutlined />} valueStyle={{ color: '#faad14' }} />
          </Card>
        </Col>
        <Col span={8}>
          <Card>
            <Statistic title="养护评分" value={4.8} suffix="/ 5.0" valueStyle={{ color: '#52c41a' }} />
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]}>
        <Col span={12}>
          <Card title="任务完成率">
            <ReactECharts option={gaugeOption} style={{ height: '300px' }} />
          </Card>
        </Col>
        <Col span={12}>
          <Card title="活跃度记录">
            <ReactECharts option={barOption} style={{ height: '300px' }} />
          </Card>
        </Col>
      </Row>
    </div>
  );
};

import { ClockCircleOutlined } from '@ant-design/icons';
export default MyAchievements;
