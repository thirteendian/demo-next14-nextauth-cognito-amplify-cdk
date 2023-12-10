package com.myorg;

import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.services.cognito.*;
import software.constructs.Construct;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;

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


    }
}
