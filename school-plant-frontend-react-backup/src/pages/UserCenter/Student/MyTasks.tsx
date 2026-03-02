import React, { useState } from 'react';
import { Table, Tag, Button, DatePicker, Space, message } from 'antd';
import dayjs from 'dayjs';

const MyTasks: React.FC = () => {
  const [filterDate, setFilterDate] = useState<any>(null);

  // Mock Data
  const data = [
    {
      key: '1',
      taskName: '每周浇水',
      plant: '银杏树 (001号)',
      deadline: '2026-03-05',
      status: 'pending',
    },
    {
      key: '2',
      taskName: '修剪枝叶',
      plant: '樱花树 (023号)',
      deadline: '2026-03-01',
      status: 'overdue',
      reason: '未按时提交记录'
    },
    {
      key: '3',
      taskName: '施肥',
      plant: '银杏树 (001号)',
      deadline: '2026-02-28',
      status: 'completed',
    },
  ];

  const handleComplete = (id: string) => {
    message.success('任务已标记为完成');
  };

  const columns = [
    {
      title: '任务名称',
      dataIndex: 'taskName',
      key: 'taskName',
    },
    {
      title: '关联植物',
      dataIndex: 'plant',
      key: 'plant',
    },
    {
      title: '截止时间',
      dataIndex: 'deadline',
      key: 'deadline',
      render: (text: string) => (
        <span className={dayjs(text).isBefore(dayjs()) ? 'text-red-500 font-bold' : ''}>
          {text}
        </span>
      )
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (status: string, record: any) => {
        if (status === 'pending') return <Tag color="orange">待完成</Tag>;
        if (status === 'completed') return <Tag color="green">已完成</Tag>;
        if (status === 'overdue') return (
          <Tag color="red" title={record.reason}>
            已逾期 ({record.reason})
          </Tag>
        );
        return status;
      }
    },
    {
      title: '操作',
      key: 'action',
      render: (_: any, record: any) => (
        record.status === 'pending' && (
          <Button type="primary" size="small" onClick={() => handleComplete(record.key)}>
            确认完成
          </Button>
        )
      ),
    },
  ];

  return (
    <div>
      <div className="flex justify-between items-center mb-4">
        <h2 className="text-xl font-bold">我的养护任务</h2>
        <Space>
          <span>截止时间筛选:</span>
          <DatePicker onChange={(date) => setFilterDate(date)} />
        </Space>
      </div>
      <Table columns={columns} dataSource={data} pagination={{ pageSize: 5 }} />
    </div>
  );
};

export default MyTasks;
