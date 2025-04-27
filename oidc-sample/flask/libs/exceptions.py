class CustomError(Exception):
    status_code = 400
    def __init__(self, message="An error occurred", status_code=None):
        self.message = message
        if status_code:
            self.status_code = status_code
        super().__init__(self.message)

class NotFoundError(CustomError):
    def __init__(self, message="Not Found"):
        self.message = message
        super().__init__(self.message, 404)

class BadRequestError(CustomError):
    def __init__(self, message="Bad Request"):
        self.message = message
        super().__init__(self.message, 400)

class UnauthorizedError(CustomError):
    def __init__(self, message="Unauthorized"):
        self.message = message
        super().__init__(self.message, 401)

class ForbiddenError(CustomError):
    def __init__(self, message="Forbidden"):
        self.message = message
        super().__init__(self.message, 403)
