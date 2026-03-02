import React, { useState, useEffect } from 'react';
import { Card, List, Tag, Button, Modal, Form, Input, Upload, message, Rate } from 'antd';
import { UploadOutlined, CameraOutlined } from '@ant-design/icons';
import { getAbnormalityList, resolveAbnormality } from '@/api/abnormality';
import dayjs from 'dayjs';
import { connectWebSocket, disconnectWebSocket } from '@/utils/websocket';

const { TextArea } = Input;

const MaintainerTask: React.FC = () => {
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(false);
  const [isModalVisible, setIsModalVisible] = useState(false);
  const [currentRecord, setCurrentRecord] = useState<any>(null);
  const [fileList, setFileList] = useState<any[]>([]);

  useEffect(() => {
    // Mock Maintainer ID
    connectWebSocket(2); 
    return () => disconnectWebSocket();
  }, []);

  const fetchData = async () => {
    setLoading(true);
    try {
      // Maintainer ID usually from token/context, here mock or use filter
      // The API backend handles "My Tasks" if we don't pass ID but rely on token role
      const res: any = await getAbnormalityList({ page: 1, size: 20, status: 'ASSIGNED' }); 
      setData(res.records);
    } catch (error) {
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const handleResolve = (record: any) => {
    setCurrentRecord(record);
    setIsModalVisible(true);
  };

  const onFinish = async (values: any) => {
    const formData = new FormData();
    formData.append('id', currentRecord.id);
    formData.append('resolution', values.resolution);
    formData.append('materials', values.materials);
    formData.append('evaluation', values.evaluation); // Assume backend takes string or int
    fileList.forEach(file => {
      formData.append('images', file.originFileObj);
    });

    try {
      await resolveAbnormality(formData);
      message.success('处理提交成功');
      setIsModalVisible(false);
      setFileList([]);
      fetchData();
    } catch (error) {
      console.error(error);
    }
  };

  return (
    <div className="p-4 bg-gray-100 min-h-screen">
      <h2 className="text-xl font-bold mb-4 text-primary">我的任务列表</h2>
      <List
        grid={{ gutter: 16, column: 1, xs: 1, sm: 2, md: 3 }}
        dataSource={data}
        loading={loading}
        renderItem={(item: any) => (
          <List.Item>
            <Card 
              title={`工单 #${item.id}`} 
              extra={<Tag color="processing">处理中</Tag>}
              actions={[
                <Button type="primary" onClick={() => handleResolve(item)}>立即处理</Button>
              ]}
            >
              <p><strong>类型:</strong> {item.abnormalityType}</p>
              <p><strong>描述:</strong> {item.description}</p>
              <p><strong>分派时间:</strong> {dayjs(item.assignedAt).format('MM-DD HH:mm')}</p>
              {/* Calculate 48h deadline */}
              <p className="text-red-500">
                <strong>截止:</strong> {dayjs(item.assignedAt).add(48, 'hour').format('MM-DD HH:mm')}
              </p>
            </Card>
          </List.Item>
        )}
      />

      <Modal
        title="填写处理结果"
        open={isModalVisible}
        onCancel={() => setIsModalVisible(false)}
        footer={null}
      >
        <Form onFinish={onFinish} layout="vertical">
          <Form.Item label="处理方法" name="resolution" rules={[{ required: true }]}>
            <TextArea rows={3} placeholder="请描述处理过程" />
          </Form.Item>
          <Form.Item label="使用材料" name="materials" rules={[{ required: true }]}>
            <Input placeholder="例如：杀虫剂、肥料" />
          </Form.Item>
          <Form.Item label="效果评估" name="evaluation" initialValue={5}>
            <Rate />
          </Form.Item>
          <Form.Item label="处理后照片">
             <Upload 
                listType="picture-card"
                fileList={fileList}
                onChange={({ fileList }) => setFileList(fileList)}
                beforeUpload={() => false}
             >
               <div>
                 <CameraOutlined />
                 <div style={{ marginTop: 8 }}>拍照/上传</div>
               </div>
             </Upload>
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit" block size="large">提交完成</Button>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default MaintainerTask;
