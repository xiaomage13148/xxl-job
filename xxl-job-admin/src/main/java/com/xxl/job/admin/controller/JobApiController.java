package com.xxl.job.admin.controller;

import com.xxl.job.admin.controller.annotation.PermissionLimit;
import com.xxl.job.admin.core.conf.XxlJobAdminConfig;
import com.xxl.job.admin.core.model.XxlJobGroup;
import com.xxl.job.admin.core.model.XxlJobInfo;
import com.xxl.job.admin.core.model.XxlJobUser;
import com.xxl.job.admin.dao.XxlJobGroupDao;
import com.xxl.job.admin.dao.XxlJobUserDao;
import com.xxl.job.admin.service.XxlJobService;
import com.xxl.job.core.biz.AdminBiz;
import com.xxl.job.core.biz.model.HandleCallbackParam;
import com.xxl.job.core.biz.model.RegistryParam;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.util.GsonTool;
import com.xxl.job.core.util.XxlJobRemotingUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Created by xuxueli on 17/5/10.
 */
@Controller
@RequestMapping("/api")
public class JobApiController {

    @Resource
    private AdminBiz adminBiz;

    @Resource
    private XxlJobService xxlJobService;

    @Resource
    private XxlJobUserDao xxlJobUserDao;

    @Resource
    private XxlJobGroupDao xxlJobGroupDao;

    private XxlJobUser xxlJobUser;

    @PostConstruct
    public void setXxlJobUser() {
        String username = "admin";
        xxlJobUser = xxlJobUserDao.loadByUserName(username);
    }

    /**
     * api
     *
     * @param uri
     * @param data
     * @return
     */
    @RequestMapping("/{uri}")
    @ResponseBody
    @PermissionLimit(limit=false)
    public ReturnT<String> api(HttpServletRequest request, @PathVariable("uri") String uri, @RequestBody(required = false) String data) {

        // valid
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "invalid request, HttpMethod not support.");
        }
        if (uri==null || uri.trim().length()==0) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "invalid request, uri-mapping empty.");
        }
        if (XxlJobAdminConfig.getAdminConfig().getAccessToken()!=null
                && XxlJobAdminConfig.getAdminConfig().getAccessToken().trim().length()>0
                && !XxlJobAdminConfig.getAdminConfig().getAccessToken().equals(request.getHeader(XxlJobRemotingUtil.XXL_JOB_ACCESS_TOKEN))) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "The access token is wrong.");
        }

        // services mapping
        if ("callback".equals(uri)) {
            List<HandleCallbackParam> callbackParamList = GsonTool.fromJson(data, List.class, HandleCallbackParam.class);
            return adminBiz.callback(callbackParamList);
        } else if ("registry".equals(uri)) {
            RegistryParam registryParam = GsonTool.fromJson(data, RegistryParam.class);
            return adminBiz.registry(registryParam);
        } else if ("registryRemove".equals(uri)) {
            RegistryParam registryParam = GsonTool.fromJson(data, RegistryParam.class);
            return adminBiz.registryRemove(registryParam);
        } else {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "invalid request, uri-mapping("+ uri +") not found.");
        }

    }


    @PostMapping("/getGroupId")
    @ResponseBody
    @PermissionLimit(limit = false)
    public ReturnT<String> getGroupId(@RequestBody XxlJobGroup jobGroup) {
        XxlJobGroup group = xxlJobGroupDao.findByName(jobGroup.getAppname());
        if (group == null) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "执行器为空");
        }
        return new ReturnT<String>(String.valueOf(group.getId()));
    }

    @PostMapping("/addJob")
    @ResponseBody
    @PermissionLimit(limit = false)
    public ReturnT<String> addJobInfo(@RequestBody XxlJobInfo jobInfo) {
        return xxlJobService.customAdd(jobInfo, xxlJobUser);
    }

    @PostMapping("/updateJob")
    @ResponseBody
    @PermissionLimit(limit = false)
    public ReturnT<String> updateJobCron(@RequestBody XxlJobInfo jobInfo) {
        return xxlJobService.update(jobInfo, xxlJobUser);
    }

    @PostMapping("/removeJobById")
    @ResponseBody
    @PermissionLimit(limit = false)
    public ReturnT<String> removeJobById(@RequestBody XxlJobInfo jobInfo) {
        return xxlJobService.remove(jobInfo.getId());
    }

    @PostMapping("/removeJobByCondition")
    @ResponseBody
    @PermissionLimit(limit = false)
    public ReturnT<String> removeJobByCondition(@RequestBody XxlJobInfo jobInfo) {
        return xxlJobService.customRemove(jobInfo);
    }

    @PostMapping("/startJob")
    @ResponseBody
    @PermissionLimit(limit = false)
    public ReturnT<String> startJob(@RequestBody XxlJobInfo jobInfo) {
        return xxlJobService.start(jobInfo.getId());
    }

    @PostMapping("/stopJob")
    @ResponseBody
    @PermissionLimit(limit = false)
    public ReturnT<String> stopJob(@RequestBody XxlJobInfo jobInfo) {
        return xxlJobService.stop(jobInfo.getId());
    }

    @PostMapping("/trigger")
    @ResponseBody
    @PermissionLimit(limit = false)
    public ReturnT<String> trigger(@RequestBody XxlJobInfo jobInfo) {
        return xxlJobService.customTrigger(xxlJobUser, jobInfo.getId(), null, null);
    }

}
