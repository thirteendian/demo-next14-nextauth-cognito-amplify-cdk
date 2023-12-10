import {getServerSession, NextAuthOptions} from "next-auth";
import {GetServerSidePropsContext, NextApiRequest, NextApiResponse} from "next";
import CredentialsProvider from "next-auth/providers/credentials"
import createUserPool from "@/app/utils/cognitoUserPool";
import {AuthenticationDetails, CognitoUser} from "amazon-cognito-identity-js";

export const authOptions: NextAuthOptions = {
    providers: [
        CredentialsProvider({
            credentials: {
                username: {label: "Username", type: "text"},
                password: {label: "Password", type: 'password'}
            },
            async authorize(credentials?) {
                if (!credentials || !credentials.username || !credentials.password) {
                    throw new Error("Username and password are not defined correctly")
                }
                const Userpool = await createUserPool()
                const cognitoUser = new CognitoUser({
                    Username: credentials.username,
                    Pool: Userpool,
                })

                const authenticationDetails = new AuthenticationDetails({
                    Username: credentials.username,
                    Password: credentials.password,
                })
                return new Promise((resolve, reject) => {
                    cognitoUser.authenticateUser(authenticationDetails, {
                        onSuccess: (result) => {
                            console.log("Cognito Login Success: ", result)
                            resolve({
                                id: credentials.username,
                                email: credentials.username
                            });
                        },
                        onFailure: (err) => {
                            console.log("Cognito Login Failed: ", err)
                            if (err.code === "UserNotConfirmedException") {
                                resolve({
                                    id: credentials.username,
                                    email: "NOTVERFIED"
                                })
                            }
                            reject(new Error(err.message) || "Cognito Email Auth Failed!")
                        }
                    })
                })
            }
        }),
    ],
    pages:{
        signIn:'/login'
    }
}

export function getSession(...args: [GetServerSidePropsContext["req"], GetServerSidePropsContext["res"]] | [NextApiRequest, NextApiResponse] | []) {
    return getServerSession(...args, authOptions)
}