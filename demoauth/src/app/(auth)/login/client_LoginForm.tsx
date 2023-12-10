'use client'
import React, {FormEvent, useState} from 'react';
import {signIn} from "next-auth/react";

function ClientLoginForm() {
    const [username, setUsername] = useState("")
    const [password, setPassword]= useState("")

    const handleSubmit=async (e:FormEvent<HTMLFormElement>)=>{
        e.preventDefault()
        try{
            const result = signIn("credentials",{username,password, callbackUrl:"/"})
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
                <button type={"submit"}>Login</button>
            </form>
        </div>
    );
}

export default ClientLoginForm;