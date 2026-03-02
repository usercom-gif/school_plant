import React from 'react';
import ReactECharts from 'echarts-for-react';
import { Card, Row, Col, Table, Tag, Button } from 'antd';

const MaintainerRecords: React.FC = () => {
  // Chart: Abnormality Types
  const pieOption = {
    title: { text: '处理异常类型占比', left: 'center' },
    tooltip: { trigger: 'item' },
    legend: { orient: 'vertical', left: 'left' },
    series: [
      {
        name: 'Access From',
        type: 'pie',
        radius: '50%',
        data: [
          { value: 10, name: '病虫害' },
          { value: 5, name: '缺水' },
          { value: 2, name: '人为损坏' },
          { value: 3, name: '其他' },
        ],
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  };

  const columns = [
    { title: 'ID', dataIndex: 'id', key: 'id' },
    { title: '异常类型', dataIndex: 'type', key: 'type' },
    { title: '处理时间', dataIndex: 'time', key: 'time' },
    { title: '耗时', dataIndex: 'duration', key: 'duration' },
    { title: '状态', dataIndex: 'status', key: 'status', render: () => <Tag color="green">已解决</Tag> },
  ];

  const data = [
    { key: 1, id: '#101', type: '病虫害', time: '2026-03-01', duration: '2小时', status: 'resolved' },
    { key: 2, id: '#105', type: '缺水', time: '2026-03-02', duration: '30分钟', status: 'resolved' },
  ];

  return (
    <div>
      <h2 className="text-xl font-bold mb-6">我的异常处理记录</h2>
      
      <Row gutter={[16, 16]} className="mb-8">
        <Col span={12}>
          <Card>
            <ReactECharts option={pieOption} />
          </Card>
        </Col>
        <Col span={12}>
          <Card title="处理效率">
            <div className="text-center p-8">
              <div className="text-4xl font-bold text-green-600 mb-2">98%</div>
              <div className="text-gray-500">按时完成率</div>
            </div>
          </Card>
        </Col>
      </Row>

      <Card title="历史记录">
        <Table columns={columns} dataSource={data} />
      </Card>
    </div>
  );
};

export default MaintainerRecords;
