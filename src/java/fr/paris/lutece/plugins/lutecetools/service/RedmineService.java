package fr.paris.lutece.plugins.lutecetools.service;

import com.taskadapter.redmineapi.*;
import com.taskadapter.redmineapi.bean.*;
import fr.paris.lutece.plugins.lutecetools.business.Component;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RedmineService implements ComponentInfoFiller
{

    public static final String REDMINE_STATUS = "redmineStatus";
    private static final String SERVICE_NAME = "Redmine Info filler service registered";
    private static final String REDMINE_URL = AppPropertiesService.getProperty( "lutecetools.redmine.url" );
    private static final String REDMINE_API_KEY = AppPropertiesService.getProperty( "lutecetools.redmine.apiKey" );
    private static final int LIMIT = 100; // Maximum limit as per Redmine API

    private final IssueManager issueManager;
    private final ProjectManager projectManager;

    public RedmineService( )
    {
        // Initialize RedmineManager and managers once in the constructor
        RedmineManager redmineManager = RedmineManagerFactory.createWithApiKey( REDMINE_URL, REDMINE_API_KEY );
        this.issueManager = redmineManager.getIssueManager( );
        this.projectManager = redmineManager.getProjectManager( );
    }

    @Override
    public void fill( Component component, StringBuilder sbLogs )
    {
        String strJiraKey = component.get( Component.JIRA_KEY );
        String snapshotVersion = component.get( Component.SNAPSHOT_VERSION );
        component.set( REDMINE_STATUS, 1 );

        if ( strJiraKey == null )
        {
            sbLogs.append( "\n*** ERROR *** No Redmine key defined for component " ).append( component.getArtifactId( ) );
            return;
        }

        try
        {
            Project project = projectManager.getProjectByKey( strJiraKey.toLowerCase( ) );

            // Fetch open issues
            List<Issue> openIssues = fetchIssuesByStatus( project.getId( ), "open" );
            component.set( "redmineOpenIssuesCount", openIssues.size( ) );

            // Fetch closed issues and filter based on snapshotVersion
            List<Issue> closedIssues = fetchIssuesByStatus( project.getId( ), "closed" );
            List<Issue> filteredClosedIssues = closedIssues.stream( ).filter( issue -> {
                String targetVersion = issue.getTargetVersion( ) != null ? issue.getTargetVersion( ).getName( ) : null;
                return targetVersion != null && targetVersion.equals( snapshotVersion.replace( "-SNAPSHOT", "" ) );
            } ).collect( Collectors.toList( ) );

            component.set( "redmineClosedIssuesCount", filteredClosedIssues.size( ) );

            if ( AppLogService.isDebugEnabled( ) )
            {
                StringBuilder sbDebug = new StringBuilder( );
                sbDebug.append( "RedmineService - Project " ).append( strJiraKey ).append( " OpenIssues : " ).append( openIssues.size( ) )
                        .append( " ClosedIssues : " ).append( filteredClosedIssues.size( ) );
                AppLogService.debug( sbDebug.toString( ) );
            }
        }
        catch( RedmineException e )
        {
            sbLogs.append( "\n*** ERROR *** Error accessing Redmine: " ).append( e.getMessage( ) );
        }
    }

    private List<Issue> fetchIssuesByStatus( int projectId, String statusId ) throws RedmineException
    {
        List<Issue> allIssues = new ArrayList<>( );
        int offset = 0;

        while ( true )
        {
            Params params = new Params( ).add( "project_id", String.valueOf( projectId ) ).add( "limit", String.valueOf( LIMIT ) )
                    .add( "offset", String.valueOf( offset ) ).add( "status_id", statusId );

            List<Issue> issues = issueManager.getIssues( params ).getResults( );

            if ( issues.isEmpty( ) )
            {
                break; // Exit the loop if there are no more issues
            }

            allIssues.addAll( issues );
            offset += LIMIT; // Increment offset for the next page
        }

        return allIssues;
    }

    @Override
    public String getName( )
    {
        return SERVICE_NAME;
    }
}