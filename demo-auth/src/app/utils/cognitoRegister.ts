'use server'
import createUserPool from "@/app/utils/cognitoUserPool";
import {CognitoUserAttribute} from "amazon-cognito-identity-js";

interface cognitoRegisterProps{
    username:string;
    password:string;
    familyName:string;
}
export default async function cognitoRegister({username,password,familyName}:cognitoRegisterProps){
    return new Promise(async (resolve,reject)=>{
        const UserPool = await createUserPool()
        const attributeList=[
            new CognitoUserAttribute({Name:"email",Value:username}),
            new CognitoUserAttribute({Name:"family_name",Value:familyName})
        ]

        UserPool.signUp(username,password,attributeList,[],(err, result)=>{
            if(err){
                console.log("Cognito Register Error", err)
                reject(new Error(err.message)||"Cognito Register Failed!")
            }
            console.log("Cognito registration result: ", result)
            const response={
                userConfirmed:result?.userConfirmed,
                userSub: result?.userSub
            }
            //OWN DATABASE
            resolve(response)
        })
    })
}