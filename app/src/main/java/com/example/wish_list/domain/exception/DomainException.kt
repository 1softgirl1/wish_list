package com.example.wish_list.domain.exception

open class DomainException(message: String) : IllegalStateException(message)

class NotFoundException(message: String) : DomainException(message)

class ValidationException(message: String) : DomainException(message)

class AccessDeniedException(message: String) : DomainException(message)
