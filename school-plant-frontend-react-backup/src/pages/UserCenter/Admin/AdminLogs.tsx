import React from 'react';
import { Table, Tag, Button, DatePicker, Space, Input } from 'antd';

const AdminLogs: React.FC = () => {
  // Mock Data
  const data = [
    { key: 1, action: '审核通过', target: '认养申请 #1024', operator: 'admin', time: '2026-03-01 10:00', result: 'Success' },
    { key: 2, action: '分派工单', target: '异常 #55', operator: 'admin', time: '2026-03-01 11:30', result: 'Success' },
    { key: 3, action: '驳回申请', target: '认养申请 #1025', operator: 'admin', time: '2026-03-02 09:15', result: 'Success' },
  ];

  const columns = [
    { title: '操作时间', dataIndex: 'time', key: 'time' },
    { title: '操作人', dataIndex: 'operator', key: 'operator' },
    { title: '操作内容', dataIndex: 'action', key: 'action' },
    { title: '操作对象', dataIndex: 'target', key: 'target' },
    { title: '结果', dataIndex: 'result', key: 'result', render: (text: string) => <Tag color="green">{text}</Tag> },
  ];

  return (
    <div>
      <div className="flex justify-between items-center mb-4">
        <h2 className="text-xl font-bold">系统操作日志</h2>
        <Space>
          <Input.Search placeholder="搜索日志内容" />
          <Button>导出日志</Button>
        </Space>
      </div>
      <Table columns={columns} dataSource={data} />
    </div>
  );
};

export default AdminLogs;
