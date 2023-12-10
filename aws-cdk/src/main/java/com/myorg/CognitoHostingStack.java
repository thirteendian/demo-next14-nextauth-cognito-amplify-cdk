package com.myorg;

import software.amazon.awscdk.*;
import software.amazon.awscdk.services.amplify.alpha.*;
import software.amazon.awscdk.services.amplify.alpha.App;
import software.amazon.awscdk.services.codebuild.BuildSpec;
import software.amazon.awscdk.services.cognito.*;
import software.constructs.Construct;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
// import software.amazon.awscdk.Duration;
// import software.amazon.awscdk.services.sqs.Queue;

public class CognitoHostingStack extends Stack {
    public CognitoHostingStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public CognitoHostingStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        UserPool userPool = UserPool.Builder.create(this,"demo-userpool")
                .userPoolName("demo-userpool")
                .removalPolicy(RemovalPolicy.DESTROY)
                .signInAliases(SignInAliases.builder()
                        .email(true)
                        .build())
                .selfSignUpEnabled(true)
                .autoVerify(AutoVerifiedAttrs.builder()
                        .email(true)
                        .build())
                .userVerification(UserVerificationConfig.builder()
                        .emailSubject("Please verify your demo email !")
                        .emailBody("Thanks for registration! Your code is {####}")
                        .emailStyle(VerificationEmailStyle.CODE)
                        .build())
                .standardAttributes(StandardAttributes.builder()
                        .email(StandardAttribute.builder()
                                .required(true)
                                .mutable(true)
                                .build())
                        .familyName(StandardAttribute.builder()
                                .required(true)
                                .mutable(false)
                                .build())
                        .build())
                .customAttributes(Map.of(
                        "created_at",new DateTimeAttribute()
                ))
                .build();

        UserPoolClient client =userPool.addClient("demo-userpoolclient", UserPoolClientOptions.builder()
                        .userPoolClientName("demo-userpoolclient")
                        .generateSecret(false)
                        .authFlows(AuthFlow.builder()
                                .userSrp(true)
                                .userPassword(true)
                                .build())
                .build());

        userPool.addDomain("yyxdemodomain", UserPoolDomainOptions.builder()
                        .cognitoDomain(CognitoDomainOptions.builder()
                                .domainPrefix("yyxdemodomain")
                                .build())
                .build());

        CfnOutput.Builder.create(this,"COGNITO_ID=").value(userPool.getUserPoolId()).build();
        CfnOutput.Builder.create(this,"COGNITO_CLIENT_ID=").value(client.getUserPoolClientId()).build();
        //issue URL


        App amplifyApp = App.Builder.create(this,"demo-amplify-hosting")
                .appName("demo-amplify-hosting")
                .sourceCodeProvider(GitHubSourceCodeProvider.Builder.create()
                        .owner("thirteendian")
                        .repository("demo-next14-nextauth-cognito-amplify-cdk")
                        .oauthToken(SecretValue.secretsManager("demo-amplify-hosting"))
                        .build())
                .autoBranchDeletion(true)
                .platform(Platform.WEB_COMPUTE)//set to be web compute if SSR
                .buildSpec(BuildSpec.fromObjectToYaml(
                        new LinkedHashMap<>() {{
                            put("version", "1.0");
                            put("applications", List.of(
                                    new LinkedHashMap<>() {{
                                        put("appRoot", "demoauth");
                                        put("frontend", new LinkedHashMap<>() {{
                                            put("buildPath", "demoauth");
                                            put("phases", new LinkedHashMap<>() {{
                                                put("preBuild", new LinkedHashMap<>() {{
                                                    put("commands", List.of(
                                                            "npm ci"
                                                    )); //clean install
                                                }});
                                                put("build", new LinkedHashMap<>() {{
                                                    put("commands", List.of(
                                                            "npm run build",
                                                            "echo \"NEXTAUTH_SECRET=17cM2QJHbi9VuO4Vhqt6COMIaqXMV8Z5lU1NCZ325Qg=\" >> .env.production",
                                                            """
                                                                    if [ "$AWS_BRANCH" = "main" ]; then
                                                                      echo "NEXTAUTH_URL=https://main.${AWS_APP_ID}.amplifyapp.com/" >> .env.production
                                                                    elif [ "$AWS_BRANCH" = "dev" ]; then
                                                                      echo "NEXTAUTH_URL=https://dev.${AWS_APP_ID}.amplifyapp.com/" >> .env.production
                                                                    fi
                                                            """,
                                                            "echo \"COGNITO_ID="+userPool.getUserPoolId()+"\" >> .env.production",
                                                            "echo \"COGNITO_CLIENT_ID"+client.getUserPoolClientId()+"\" >> .env.production"
                                                            )); //set postBuild if needed
                                                }});
                                            }});
                                            put("artifacts", new LinkedHashMap<>() {{
                                                put("files", List.of("**/*"));
                                                put("baseDirectory", ".next");
                                            }});
                                            put("cache", new LinkedHashMap<>() {{
                                                put("paths", List.of("node_modules/**/*", ".next/cache/**/*"));
                                            }});
                                        }});
                                    }}
                            ));
                        }}
                ))
                .build();

        amplifyApp.addCustomRule(CustomRule.Builder.create()
                        .source("/<*>")
                        .target("/index.html")
                        .status(RedirectStatus.NOT_FOUND_REWRITE)
                .build());
        amplifyApp.addEnvironment("COGNITO_ID",userPool.getUserPoolId())
                .addEnvironment("COGNITO_CLIENT_ID",client.getUserPoolClientId())
                .addEnvironment("_CUSTOM_IMAGE","amplify:al2023")
                .addEnvironment("_LIVE_UPDATES","[{\"pkg\":\"next-version\",\"type\":\"internal\",\"version\":\"latest\"}]");

        Branch main = amplifyApp.addBranch("main", BranchOptions.builder()
                        .stage("PRODUCTION")
                .build());
        Branch dev = amplifyApp.addBranch("dev", BranchOptions.builder()
                        .stage("DEVELOPMENT")
                        .performanceMode(true)
                .build());
    }
}
