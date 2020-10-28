package com.jin10.spidermanage.service.impl;


import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jin10.spidermanage.bean.BaseResponse;
import com.jin10.spidermanage.bean.label.InsertBody;
import com.jin10.spidermanage.bean.label.InsertBodyTest;
import com.jin10.spidermanage.bean.label.KV;
import com.jin10.spidermanage.bean.label.Search;
import com.jin10.spidermanage.bean.spider.XxlJobResponse;
import com.jin10.spidermanage.dto.AddLabelDTO;
import com.jin10.spidermanage.dto.LabelDTO;
import com.jin10.spidermanage.entity.ImgUrl;
import com.jin10.spidermanage.entity.Label;

import com.jin10.spidermanage.entity.Link;
import com.jin10.spidermanage.manage.ImgUrlCache;
import com.jin10.spidermanage.mapper.LabelMapper;
import com.jin10.spidermanage.service.ImgUrlService;
import com.jin10.spidermanage.service.LabelService;
import com.jin10.spidermanage.service.LinkService;
import com.jin10.spidermanage.util.RegularUtil;
import com.jin10.spidermanage.util.UploadFile;
import com.jin10.spidermanage.util.XxlJobUtil;
import org.apache.commons.lang.StringUtils;
import org.quartz.CronExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
;
import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LabelServiceImpl extends ServiceImpl<LabelMapper, Label> implements LabelService {

    @Value("${xxl.job.admin.addresses}")
    private String adminAddresses;
    @Value("${xxl.job.executor.appname}")
    private String executorAppname;


    @Resource
    private LabelMapper labelMapper;

    @Autowired
    private LinkService linkService;

    @Autowired
    private ImgUrlService imgUrlService;

    @Override
    @Transactional
    public BaseResponse add(InsertBody body) {
        QueryWrapper<Label> condition = new QueryWrapper<>();
        ImgUrlCache imgUrlCache = ImgUrlCache.getInstance();
        condition.eq("label_name", body.getLabelName()).last("limit 1");
        Integer integer = baseMapper.selectCount(condition);
        if (integer != 0) {
            return BaseResponse.error("标签已存在");
        }
        labelMapper.addElement(body);
        ArrayList<Link> linkList = new ArrayList<>();
        ArrayList<ImgUrl> imgList = new ArrayList<>();
        boolean link = true;
        boolean img = true;
        if (body.getSpiderLink().size() > 0) {
            List<String> spiderLink = body.getSpiderLink();
            for (int i = 0; i < spiderLink.size(); i++) {
                String s = spiderLink.get(i);
                if (StringUtils.isNotBlank(s)) {
                    linkList.add(new Link(body.getLid(), body.getSpiderLink().get(i)));
                }
            }
            if (!linkList.isEmpty()) {
                link = linkService.saveOrUpdateBatch(linkList);
            }
        }
        if (body.getImg().size() > 0) {
            for (int i = 0; i < body.getImg().size(); i++) {
                String path = imgUrlCache.getElement(body.getImg().get(i));
                if (StringUtils.isNotBlank(path)) {
                    imgList.add(new ImgUrl(body.getLid(), body.getImg().get(i), path));
                }

            }
            if (!imgList.isEmpty()) {
                img = imgUrlService.saveOrUpdateBatch(imgList);
            }
        }
        if (link && img) {
            try {
                Map<String, String> requestInfo = new HashMap<>();

                // 执行器主键ID
                requestInfo.put("jobGroup", String.valueOf(body.getExecutorId()));

                // 任务执行CRON表达式
                requestInfo.put("jobCron", body.getCron());

                // 任务描述
                requestInfo.put("jobDesc", body.getDemandDesc());

                // 负责人
                requestInfo.put("author", "admin");

                // 执行器路由策略
                requestInfo.put("executorRouteStrategy", "RANDOM");

                // 执行器，任务Handler名称
                requestInfo.put("executorHandler", "Job");

                // todo 执行器，任务参数
                requestInfo.put("executorParam", body.getParam());

                // 阻塞处理策略
                requestInfo.put("executorBlockStrategy", "SERIAL_EXECUTION");
                // 任务执行超时时间，单位秒
                requestInfo.put("executorTimeout", "0");
                // 失败重试次数
                requestInfo.put("executorFailRetryCount", "1");
                // GLUE类型	#com.xxl.job.core.glue.GlueTypeEnum
                requestInfo.put("glueType", "BEAN");
                // GLUE备注
                requestInfo.put("glueRemark", "GLUE代码初始化");
                // 调度状态：0-停止，1-运行
                requestInfo.put("triggerStatus", "0");
                // 上次调度时间
                requestInfo.put("triggerLastTime", "0");
                // 下次调度时间
                requestInfo.put("triggerNextTime", "0");
                XxlJobResponse xxlJobResponse = XxlJobUtil.addJob(adminAddresses, requestInfo);
                if (xxlJobResponse.getCode() == 200) {
                    body.setExecutorId(Integer.valueOf(xxlJobResponse.getContent().toString()));
                    LambdaUpdateWrapper<Label> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
                    lambdaUpdateWrapper.eq(Label::getId, body.getLid()).set(Label::getTaskId, body.getExecutorId());
                    baseMapper.update(null, lambdaUpdateWrapper);
                } else {
                    throw new Exception("调用xxl-job-admin-add接口失败！");
                }
            } catch (Exception e) {
                e.printStackTrace();
                labelMapper.deleteById(body.getLid());        //数据库回退
                return BaseResponse.error("执行器ID不存在");
            }
            return BaseResponse.ok(labelMapper.getById(body.getLid()));
        }
        return BaseResponse.error("添加失败");

    }

    /**
     * @param body
     * @return
     */
    @Override
    public BaseResponse addKV(InsertBodyTest body) {
        if (CronExpression.isValidExpression(body.getCron())) {    //校验cron表达式的合法性
            QueryWrapper<Label> condition = new QueryWrapper<>();
            ImgUrlCache imgUrlCache = ImgUrlCache.getInstance();
            condition.eq("label_name", body.getLabelName()).last("limit 1");
            Integer integer = baseMapper.selectCount(condition);
            if (integer != 0) {
                return BaseResponse.error("标签已存在");
            }
            StringBuffer stringBuffer = new StringBuffer();
            for (KV kv : body.getParam()) {
                stringBuffer.append(String.format("%s=%s", kv.getKey(), kv.getValue()) + "&");
            }
            InsertBody insertBody = new InsertBody(body);
            insertBody.setParam(stringBuffer.toString());
            labelMapper.addElement(insertBody);
            ArrayList<Link> linkList = new ArrayList<>();
            ArrayList<ImgUrl> imgList = new ArrayList<>();
            boolean link = true;
            boolean img = true;
            if (body.getSpiderLink().size() > 0) {
                for (int i = 0; i < body.getSpiderLink().size(); i++) {
                    linkList.add(new Link(body.getLid(), body.getSpiderLink().get(i)));
                    link = linkService.saveOrUpdateBatch(linkList);
                }
            }
            if (body.getImg().size() > 0) {
                for (int i = 0; i < body.getImg().size(); i++) {
                    String path = imgUrlCache.getElement(body.getImg().get(i));
                    if (StringUtils.isNotBlank(path)) {
                        imgList.add(new ImgUrl(body.getLid(), body.getImg().get(i), path));
                    }
                    img = imgUrlService.saveOrUpdateBatch(imgList);
                }
            }
            if (link && img) {                                 //添加记录成功

                return BaseResponse.ok(new AddLabelDTO(body.getGid(), body.getLid()));
            }
            return BaseResponse.error("添加失败");
        }
        return BaseResponse.error("cron表达式不合法！");
    }

    @Override
    public List<LabelDTO> getAll() {
        return labelMapper.getAll();
    }

    @Override
    @Transactional
    public boolean delete(Integer id, Integer eid) {
        ImgUrlCache instance = ImgUrlCache.getInstance();
        //删除任务执行器
        if (eid >= 0) {
            try {
                JSONObject response = XxlJobUtil.deleteJob(adminAddresses, eid);
                if (response.containsKey("code") && 200 == (Integer) response.get("code")) {
                    labelMapper.deleteById(id);
                    linkService.remove(new QueryWrapper<Link>().eq("label_id", id));
                    imgUrlService.remove(new QueryWrapper<ImgUrl>().eq("label_id", id));
                    List<ImgUrl> imgUrls = imgUrlService.getBaseMapper().selectList(new QueryWrapper<ImgUrl>().eq("label_id", id));
                    for (ImgUrl i : imgUrls) {
                        UploadFile.deleteFile(i.getPath());
                        instance.delElement(i.getUrl());
                    }
                    return true;
                }
                return false;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    @Transactional
    public BaseResponse updateLabel(InsertBody body) {    //先更新调度器后更新数据库
        ImgUrlCache imgUrlCache = ImgUrlCache.getInstance();
        Label label = new Label(body);
        try {
            Map<String, String> requestInfo = new HashMap<>();
            //主键ID
            if (body.getTaskId() > 0) {
                requestInfo.put("id", String.valueOf(body.getTaskId()));
            } else {
                return BaseResponse.error("任务号ID不能为零");
            }
            // 执行器主键ID
            requestInfo.put("jobGroup", String.valueOf(body.getExecutorId()));
            // 任务执行CRON表达式
            requestInfo.put("jobCron", body.getCron());
            // 任务描述
            requestInfo.put("jobDesc", body.getDemandDesc());
            // 负责人
            requestInfo.put("author", "admin");
            // 执行器路由策略
            requestInfo.put("executorRouteStrategy", "RANDOM");
            // 执行器，任务Handler名称
            requestInfo.put("executorHandler", "Job");
            // todo 执行器，任务参数
            requestInfo.put("executorParam", body.getParam());
            // 阻塞处理策略
            requestInfo.put("executorBlockStrategy", "SERIAL_EXECUTION");
            // 任务执行超时时间，单位秒
            requestInfo.put("executorTimeout", "0");
            // 失败重试次数
            requestInfo.put("executorFailRetryCount", "1");
            // GLUE类型	#com.xxl.job.core.glue.GlueTypeEnum
            requestInfo.put("glueType", "BEAN");
            // GLUE备注
            requestInfo.put("glueRemark", "GLUE代码初始化");
            // 调度状态：0-停止，1-运行
            requestInfo.put("triggerStatus", "0");
            // 上次调度时间
            requestInfo.put("triggerLastTime", "0");
            // 下次调度时间
            requestInfo.put("triggerNextTime", "0");

            XxlJobResponse xxlJobResponse = XxlJobUtil.updateJob(adminAddresses, requestInfo);

            if (xxlJobResponse.getCode() == 200) {
                labelMapper.updateById(label);
                linkService.remove(new QueryWrapper<Link>().eq("label_id", body.getLid()));
                imgUrlService.remove(new QueryWrapper<ImgUrl>().eq("label_id", body.getLid()));
                ArrayList<Link> linkList = new ArrayList<>();
                ArrayList<ImgUrl> imgList = new ArrayList<>();
                boolean link = true;
                boolean img = true;
                if (body.getSpiderLink().size() > 0) {
                    for (int i = 0; i < body.getSpiderLink().size(); i++) {
                        linkList.add(new Link(body.getLid(), body.getSpiderLink().get(i)));
                    }
                    link = linkService.saveOrUpdateBatch(linkList);
                }
                if (body.getImg().size() > 0) {
                    for (int i = 0; i < body.getImg().size(); i++) {
                        String path = imgUrlCache.getElement(body.getImg().get(i));
                        if (StringUtils.isNotBlank(path)) {
                            imgList.add(new ImgUrl(body.getLid(), body.getImg().get(i), path));
                        }
                    }
                    if (!imgList.isEmpty()) {
                        img = imgUrlService.saveOrUpdateBatch(imgList);
                    }
                }
                return BaseResponse.ok(labelMapper.getById(body.getLid()));
            } else {
                return BaseResponse.error(xxlJobResponse.getMsg());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return BaseResponse.error("更新成功但更新调度任务失败");
        }
    }


    @Override
    public LabelDTO getById(Integer id) {
        return labelMapper.getById(id);
    }

    @Override
    public List<Search> getLabelByCondition(String condition) {

        return labelMapper.getByLabelLike(condition);
    }
}
