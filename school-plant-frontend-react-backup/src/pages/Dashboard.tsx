import React, { useState, useEffect } from 'react';
import { Card, Form, Input, Select, Button, Upload, message, List, Tag, Image, Spin } from 'antd';
import { UploadOutlined, PlusOutlined, HistoryOutlined } from '@ant-design/icons';
import { reportAbnormality, getAbnormalityList } from '@/api/abnormality';
import { useNavigate } from 'react-router-dom';
import { connectWebSocket, disconnectWebSocket } from '@/utils/websocket';

const { Option } = Select;
const { TextArea } = Input;

const Dashboard: React.FC = () => {
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);
  const [fileList, setFileList] = useState<any[]>([]);
  const [history, setHistory] = useState<any[]>([]);
  const [historyLoading, setHistoryLoading] = useState(false);
  const navigate = useNavigate();

  // Connect WS on mount
  useEffect(() => {
    // Mock user ID - in real app get from token/context
    // We assume user ID 1 for demo if not stored
    const userId = 1; 
    connectWebSocket(userId);
    return () => disconnectWebSocket();
  }, []);

  const fetchHistory = async () => {
    setHistoryLoading(true);
    try {
      const res: any = await getAbnormalityList({ page: 1, size: 5 });
      setHistory(res.records);
    } catch (error) {
      console.error(error);
    } finally {
      setHistoryLoading(false);
    }
  };

  useEffect(() => {
    fetchHistory();
  }, []);

  const onFinish = async (values: any) => {
    if (fileList.length === 0) {
      message.error('请至少上传一张照片');
      return;
    }
    setLoading(true);
    const formData = new FormData();
    formData.append('plantId', '1'); // Mock Plant ID, in real app select from list
    formData.append('type', values.type);
    formData.append('desc', values.desc);
    fileList.forEach(file => {
      formData.append('images', file.originFileObj);
    });

    try {
      const result = await reportAbnormality(formData);
      message.success('上报成功！AI建议：' + result);
      form.resetFields();
      setFileList([]);
      fetchHistory();
    } catch (error) {
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  const normFile = (e: any) => {
    if (Array.isArray(e)) {
      return e;
    }
    return e?.fileList;
  };

  const uploadProps = {
    onRemove: (file: any) => {
      const index = fileList.indexOf(file);
      const newFileList = fileList.slice();
      newFileList.splice(index, 1);
      setFileList(newFileList);
    },
    beforeUpload: (file: any) => {
      const isJpgOrPng = file.type === 'image/jpeg' || file.type === 'image/png';
      if (!isJpgOrPng) {
        message.error('只能上传 JPG/PNG 文件!');
        return Upload.LIST_IGNORE;
      }
      const isLt5M = file.size / 1024 / 1024 < 5;
      if (!isLt5M) {
        message.error('图片必须小于 5MB!');
        return Upload.LIST_IGNORE;
      }
      setFileList([...fileList, file]);
      return false;
    },
    fileList,
  };

  return (
    <div className="p-6 bg-gray-100 min-h-screen">
      <div className="max-w-4xl mx-auto grid grid-cols-1 md:grid-cols-3 gap-6">
        <div className="md:col-span-2">
          <Card title="植物异常上报" className="shadow-md">
            <Form form={form} layout="vertical" onFinish={onFinish}>
              <Form.Item label="异常类型" name="type" rules={[{ required: true, message: '请选择异常类型' }]}>
                <Select placeholder="请选择">
                  <Option value="PEST">病虫害</Option>
                  <Option value="WATER">缺水</Option>
                  <Option value="NUTRIENT">营养不良</Option>
                  <Option value="OTHER">其他</Option>
                </Select>
              </Form.Item>

              <Form.Item 
                label="详细描述" 
                name="desc" 
                rules={[
                  { required: true, message: '请填写描述' },
                  { min: 20, message: '描述不能少于20字' }
                ]}
              >
                <TextArea rows={4} showCount minLength={20} placeholder="请详细描述植物的异常情况（不少于20字）" />
              </Form.Item>

              <Form.Item label="现场照片" required>
                <Upload {...uploadProps} listType="picture-card" maxCount={5}>
                  <div>
                    <PlusOutlined />
                    <div style={{ marginTop: 8 }}>上传</div>
                  </div>
                </Upload>
              </Form.Item>

              <Form.Item>
                <Button type="primary" htmlType="submit" loading={loading} block size="large" className="bg-primary hover:bg-primary-hover">
                  提交上报
                </Button>
              </Form.Item>
            </Form>
          </Card>
        </div>

        <div className="md:col-span-1">
          <Card title="最近上报记录" extra={<a onClick={() => navigate('/history')}>查看全部</a>} className="shadow-md">
            {historyLoading ? <Spin /> : (
              <List
                itemLayout="horizontal"
                dataSource={history}
                renderItem={(item: any) => (
                  <List.Item>
                    <List.Item.Meta
                      avatar={<Image width={50} src={JSON.parse(item.imageUrls || '[]')[0]} fallback="error" />}
                      title={<Tag color={item.status === 'PENDING' ? 'orange' : 'green'}>{item.status}</Tag>}
                      description={
                        <div className="text-xs text-gray-500">
                          <div>{item.abnormalityType}</div>
                          <div className="truncate w-32">{item.description}</div>
                        </div>
                      }
                    />
                  </List.Item>
                )}
              />
            )}
          </Card>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;
