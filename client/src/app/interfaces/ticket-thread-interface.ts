export interface TicketThreadInterface {
    ticketThreadId: number, 
    ticketId: number, 
    thread: string,
    createdAt: string
}

export interface CreateThreadMessageInterface {
    ticketId: number, 
    thread: string
}
