import com.atlassian.bamboo.specs.api.BambooSpec;
import com.atlassian.bamboo.specs.api.builders.BambooKey;
import com.atlassian.bamboo.specs.api.builders.BambooOid;
import com.atlassian.bamboo.specs.api.builders.applink.ApplicationLink;
import com.atlassian.bamboo.specs.api.builders.permission.PermissionType;
import com.atlassian.bamboo.specs.api.builders.permission.Permissions;
import com.atlassian.bamboo.specs.api.builders.permission.PlanPermissions;
import com.atlassian.bamboo.specs.api.builders.plan.Job;
import com.atlassian.bamboo.specs.api.builders.plan.Plan;
import com.atlassian.bamboo.specs.api.builders.plan.PlanIdentifier;
import com.atlassian.bamboo.specs.api.builders.plan.Stage;
import com.atlassian.bamboo.specs.api.builders.plan.branches.BranchCleanup;
import com.atlassian.bamboo.specs.api.builders.plan.branches.PlanBranchManagement;
import com.atlassian.bamboo.specs.api.builders.plan.configuration.ConcurrentBuilds;
import com.atlassian.bamboo.specs.api.builders.project.Project;
import com.atlassian.bamboo.specs.api.builders.repository.VcsChangeDetection;
import com.atlassian.bamboo.specs.builders.repository.bitbucket.server.BitbucketServerRepository;
import com.atlassian.bamboo.specs.builders.repository.viewer.BitbucketServerRepositoryViewer;
import com.atlassian.bamboo.specs.builders.task.CheckoutItem;
import com.atlassian.bamboo.specs.builders.task.VcsCheckoutTask;
import com.atlassian.bamboo.specs.builders.trigger.BitbucketServerTrigger;
import com.atlassian.bamboo.specs.api.builders.BambooOid;
import com.atlassian.bamboo.specs.api.builders.deployment.Deployment;
import com.atlassian.bamboo.specs.api.builders.deployment.Environment;
import com.atlassian.bamboo.specs.api.builders.deployment.ReleaseNaming;
import com.atlassian.bamboo.specs.api.builders.permission.DeploymentPermissions;
import com.atlassian.bamboo.specs.api.builders.permission.EnvironmentPermissions;
import com.atlassian.bamboo.specs.api.builders.permission.PermissionType;
import com.atlassian.bamboo.specs.api.builders.permission.Permissions;
import com.atlassian.bamboo.specs.api.builders.plan.PlanIdentifier;
import com.atlassian.bamboo.specs.builders.task.ArtifactDownloaderTask;
import com.atlassian.bamboo.specs.builders.task.CleanWorkingDirectoryTask;
import com.atlassian.bamboo.specs.builders.task.DownloadItem;
import com.atlassian.bamboo.specs.builders.task.ScriptTask;
import com.atlassian.bamboo.specs.builders.trigger.AfterSuccessfulBuildPlanTrigger;
import com.atlassian.bamboo.specs.util.BambooServer;

@BambooSpec
public class PlanSpec {
    
    public Plan plan() {
        final Plan plan = new Plan(new Project()
                .key(new BambooKey("NEW"))
                .name("NEW"),
            "New",
            new BambooKey("NEW"))
            .pluginConfigurations(new ConcurrentBuilds())
            .stages(new Stage("Default Stage")
                    .jobs(new Job("Default Job",
                            new BambooKey("JOB1"))
                            .tasks(new VcsCheckoutTask()
                                    .description("Checkout Default Repository")
                                    .checkoutItems(new CheckoutItem().defaultRepository()))))
            .linkedRepositories("githubspecs")
            

            .planBranchManagement(new PlanBranchManagement()
                    .createForVcsBranch()
                    .delete(new BranchCleanup()
                        .whenRemovedFromRepositoryAfterDays(7)
                        .whenInactiveInRepositoryAfterDays(30))
                    .notificationForCommitters()
                    .issueLinkingEnabled(false));
        return plan;
    }
    
    public PlanPermissions planPermission() {
        final PlanPermissions planPermission = new PlanPermissions(new PlanIdentifier("NEW", "NEW"))
            .permissions(new Permissions()
                    .userPermissions("admin", PermissionType.EDIT, PermissionType.VIEW_CONFIGURATION, PermissionType.VIEW, PermissionType.ADMIN, PermissionType.CLONE, PermissionType.BUILD));
        return planPermission;
    }
  
  public Deployment rootObject() {
        final Deployment rootObject = new Deployment(new PlanIdentifier("NEW", "NEW"),
            "deploy2")
            .releaseNaming(new ReleaseNaming("release-1")
                    .autoIncrement(true))
            .environments(new Environment("QA")
                    .tasks(new CleanWorkingDirectoryTask(),
                        new ArtifactDownloaderTask()
                            .description("Download release contents")
                            .artifacts(new DownloadItem()
                                    .allArtifacts(true)),
                        new ScriptTask()
                            .inlineBody("ls"))
                    .triggers(new AfterSuccessfulBuildPlanTrigger()));
        return rootObject;
    }
    
    public DeploymentPermissions deploymentPermission() {
        final DeploymentPermissions deploymentPermission = new DeploymentPermissions("deploy2")
            .permissions(new Permissions()
                    .userPermissions("admin", PermissionType.EDIT, PermissionType.VIEW_CONFIGURATION, PermissionType.VIEW, PermissionType.APPROVE_RELEASE));
        return deploymentPermission;
    }
    
    public EnvironmentPermissions environmentPermission1() {
        final EnvironmentPermissions environmentPermission1 = new EnvironmentPermissions("deploy2")
            .environmentName("QA")
            .permissions(new Permissions()
                    .userPermissions("admin", PermissionType.EDIT, PermissionType.VIEW_CONFIGURATION, PermissionType.VIEW, PermissionType.BUILD));
        return environmentPermission1;
    }
    
    public static void main(String... argv) {
        //By default credentials are read from the '.credentials' file.
        BambooServer bambooServer = new BambooServer("https://instenv-485153-abdd.instenv.internal.atlassian.com");
        final PlanSpec planSpec = new PlanSpec();
        
        final Plan plan = planSpec.plan();
        bambooServer.publish(plan);
        
        final PlanPermissions planPermission = planSpec.planPermission();
        bambooServer.publish(planPermission);
      
        final Deployment rootObject = planSpec.rootObject();
        bambooServer.publish(rootObject);
        
        final DeploymentPermissions deploymentPermission = planSpec.deploymentPermission();
        bambooServer.publish(deploymentPermission);
        
        final EnvironmentPermissions environmentPermission1 = planSpec.environmentPermission1();
        bambooServer.publish(environmentPermission1);
    }
}
