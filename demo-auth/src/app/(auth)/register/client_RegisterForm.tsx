'use client'
import React, {FormEvent, useState} from 'react';
import cognitoRegister from "@/app/utils/cognitoRegister";

function ClientLoginForm() {
    const [username, setUsername] = useState("")
    const [password, setPassword]= useState("")
    const [familyName, setFamilyName]= useState("")

    const handleSubmit=async (e:FormEvent<HTMLFormElement>)=>{
        e.preventDefault()
        const registerdata = {
            username:username,
            password:password,
            familyName:familyName
        }
        try{
            const result = await cognitoRegister(registerdata)
            console.log(result)
        }catch (err){
            console.log(err)
        }
    }
    return (
        <div>
            <form onSubmit={handleSubmit}>
                <label>username:</label>
                <input type={"text"} onChange={(e)=>{
                    setUsername(e.target.value)
                }}/>
                <label>password:</label>
                <input type={"password"} onChange={(e)=>{
                    setPassword(e.target.value)
                }}/>
                <label>familyName:</label>
                <input type={"text"} onChange={(e)=>{
                    setFamilyName(e.target.value)
                }}/>

                <button type={"submit"}>Register</button>
            </form>
        </div>
    );
}

export default ClientLoginForm;