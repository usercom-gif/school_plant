import React, { useState, useEffect } from 'react';
import { Card, Table, Tag, Button, Modal, Form, Select, Input, message, Tabs } from 'antd';
import { getAbnormalityList, assignAbnormality } from '@/api/abnormality';
import dayjs from 'dayjs';
import { connectWebSocket, disconnectWebSocket } from '@/utils/websocket';

const { Option } = Select;
const { TextArea } = Input;

const AdminDashboard: React.FC = () => {
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(false);
  const [isModalVisible, setIsModalVisible] = useState(false);
  const [currentRecord, setCurrentRecord] = useState<any>(null);
  const [form] = Form.useForm();
  const [activeTab, setActiveTab] = useState('PENDING');

  useEffect(() => {
    // Mock Admin ID
    connectWebSocket(100); 
    return () => disconnectWebSocket();
  }, []);

  const fetchData = async () => {
    setLoading(true);
    try {
      const res: any = await getAbnormalityList({ 
        page: 1, 
        size: 100, 
        status: activeTab === 'ALL' ? undefined : activeTab 
      });
      setData(res.records);
    } catch (error) {
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, [activeTab]);

  const handleAssign = (record: any) => {
    setCurrentRecord(record);
    setIsModalVisible(true);
  };

  const handleAssignSubmit = async (values: any) => {
    try {
      await assignAbnormality({
        id: currentRecord.id,
        maintainerId: values.maintainerId
      });
      message.success('分派成功');
      setIsModalVisible(false);
      fetchData();
    } catch (error) {
      console.error(error);
    }
  };

  const columns = [
    {
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
    },
    {
      title: '类型',
      dataIndex: 'abnormalityType',
      key: 'abnormalityType',
      render: (text: string) => <Tag color="blue">{text}</Tag>,
    },
    {
      title: '紧急程度',
      dataIndex: 'urgency',
      key: 'urgency',
      render: (text: string) => <Tag color={text === 'HIGH' ? 'red' : text === 'MEDIUM' ? 'orange' : 'green'}>{text || 'MEDIUM'}</Tag>,
    },
    {
      title: '描述',
      dataIndex: 'description',
      key: 'description',
      ellipsis: true,
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (status: string) => {
        let color = 'default';
        if (status === 'PENDING') color = 'orange';
        if (status === 'ASSIGNED') color = 'processing';
        if (status === 'RESOLVED') color = 'success';
        return <Tag color={color}>{status}</Tag>;
      }
    },
    {
      title: '上报时间',
      dataIndex: 'createdAt',
      key: 'createdAt',
      render: (text: string) => dayjs(text).format('YYYY-MM-DD HH:mm'),
    },
    {
      title: '操作',
      key: 'action',
      render: (_: any, record: any) => (
        record.status === 'PENDING' && (
          <Button type="primary" size="small" onClick={() => handleAssign(record)}>分派</Button>
        )
      ),
    },
  ];

  return (
    <div className="p-6 bg-gray-100 min-h-screen">
      <Card title="管理员工作台" className="shadow-md">
        <Tabs activeKey={activeTab} onChange={setActiveTab}>
          <Tabs.TabPane tab="待分派" key="PENDING" />
          <Tabs.TabPane tab="已分派" key="ASSIGNED" />
          <Tabs.TabPane tab="已完成" key="RESOLVED" />
          <Tabs.TabPane tab="全部" key="ALL" />
        </Tabs>
        
        <Table 
          columns={columns} 
          dataSource={data} 
          rowKey="id" 
          loading={loading}
          pagination={{ pageSize: 10 }}
        />
      </Card>

      <Modal
        title="分派工单"
        open={isModalVisible}
        onCancel={() => setIsModalVisible(false)}
        footer={null}
      >
        <Form form={form} onFinish={handleAssignSubmit} layout="vertical">
          <Form.Item label="选择养护员" name="maintainerId" rules={[{ required: true }]}>
            <Select placeholder="请选择">
              {/* Mock Maintainer List - In real app fetch from /user/list?role=MAINTAINER */}
              <Option value="2">养护员A (ID: 2)</Option>
              <Option value="3">养护员B (ID: 3)</Option>
            </Select>
          </Form.Item>
          <Form.Item label="备注" name="notes">
            <TextArea rows={2} />
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit" block>确认分派</Button>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default AdminDashboard;
