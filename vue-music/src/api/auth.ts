import request from './request'
// export function loginApi(data:any) {
//     return request({
//         url: '/api/login',
//         method: 'post',
//         data
//     })
// }
export interface LoginParams{
    username:string;
    password:string;
}
export interface RegisterParams{
    username:string;
    password:string;
    nickname:string;
}
export interface LoginVO{
    token:string;
    userId:number;
    username:string;
    nickname:string;
    avatar:string;
    role:number;
    expiresIn:number;
}
//Promise<LoginVO>:返回类型
export const loginApi=(data:LoginParams):Promise<LoginVO>=>{
    return request.post('/login',data)
}
export const registApi=(data:RegisterParams):Promise<any>=>{
    return request.post('/register',data)
}