
'use server'

import {CognitoUserPool} from "amazon-cognito-identity-js"

const userPoolId=process.env.COGNITO_ID
const clientId=process.env.COGNITO_CLIENT_ID

if(!userPoolId||!clientId){
    throw Error("COGNITO ENV IS NOT DEFINED!")
}

const poolData={
    UserPoolId:userPoolId,
    ClientId:clientId
}

async function createUserPool(){
    return new CognitoUserPool(poolData)
}

export default createUserPool
