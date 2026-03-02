import React from 'react';
import { List, Card, Tag, Button, Modal, message } from 'antd';
import { ExclamationCircleOutlined } from '@ant-design/icons';

const MyAdoptions: React.FC = () => {
  // Mock Data
  const data = [
    {
      id: 1,
      title: '银杏树 (001号)',
      species: '银杏',
      image: 'https://images.unsplash.com/photo-1508770857850-928925585d88?ixlib=rb-1.2.1&auto=format&fit=crop&w=500&q=60',
      date: '2025-03-01',
      status: 'active'
    },
    {
      id: 2,
      title: '樱花树 (023号)',
      species: '樱花',
      image: 'https://images.unsplash.com/photo-1522383225653-ed111181a951?ixlib=rb-1.2.1&auto=format&fit=crop&w=500&q=60',
      date: '2025-04-15',
      status: 'active'
    }
  ];

  const handleUnsubscribe = (item: any) => {
    Modal.confirm({
      title: '确认解除认养?',
      icon: <ExclamationCircleOutlined />,
      content: `您确定要放弃认养 ${item.title} 吗？此操作不可撤销。`,
      onOk() {
        message.success('解除认养申请已提交');
      },
    });
  };

  return (
    <div>
      <h2 className="text-xl font-bold mb-4">我的认养植物</h2>
      <List
        grid={{ gutter: 16, xs: 1, sm: 2, md: 3, lg: 3, xl: 4, xxl: 4 }}
        dataSource={data}
        renderItem={item => (
          <List.Item>
            <Card
              hoverable
              cover={<img alt={item.title} src={item.image} className="h-48 object-cover" />}
              actions={[
                <Button type="link" key="detail">查看详情</Button>,
                <Button type="link" danger key="cancel" onClick={() => handleUnsubscribe(item)}>解除认养</Button>
              ]}
            >
              <Card.Meta
                title={item.title}
                description={
                  <div>
                    <p>品种: {item.species}</p>
                    <p>认养时间: {item.date}</p>
                    <Tag color="green">认养中</Tag>
                  </div>
                }
              />
            </Card>
          </List.Item>
        )}
      />
    </div>
  );
};

export default MyAdoptions;
